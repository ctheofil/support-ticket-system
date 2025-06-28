
package com.example.ticket.service;

import com.example.ticket.dto.*;
import com.example.ticket.model.*;
import com.example.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class containing the business logic for ticket management operations.
 * 
 * <p>This service acts as the core business layer, handling all ticket-related
 * operations including creation, status updates, filtering, and comment management.
 * It enforces business rules such as status transition validation and maintains
 * data integrity.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Submit new support tickets</li>
 *   <li>List tickets with filtering options (status, userId, assigneeId)</li>
 *   <li>Update ticket status with validation</li>
 *   <li>Post internal or public comments to tickets</li>
 *   <li>Enforce business rules and status transitions</li>
 * </ul>
 * 
 * <p>Business Rules Enforced:</p>
 * <ul>
 *   <li>New tickets always start with OPEN status</li>
 *   <li>Prevent invalid status transitions (e.g., cannot move from closed to in_progress)</li>
 *   <li>Only public comments are visible to users</li>
 *   <li>Agents can add both public and internal comments</li>
 *   <li>All modifications update the ticket's updatedAt timestamp</li>
 * </ul>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    /**
     * Repository for ticket persistence operations.
     * Injected via constructor using Lombok's @RequiredArgsConstructor.
     */
    private final TicketRepository repository;

    /**
     * Creates a new support ticket from the provided request.
     * 
     * <p>This method handles the complete ticket creation process:</p>
     * <ul>
     *   <li>Generates a unique ticket ID (UUID)</li>
     *   <li>Sets initial status to OPEN</li>
     *   <li>Sets creation and update timestamps</li>
     *   <li>Persists the ticket to the repository</li>
     * </ul>
     * 
     * <p>Expected request format:</p>
     * <pre>
     * {
     *   "userId": "user-001",
     *   "subject": "Payment issue",
     *   "description": "I was charged twice for the same order."
     * }
     * </pre>
     * 
     * @param request the ticket creation request containing user ID, subject, and description
     * @return the newly created ticket with generated ID and timestamps
     * @throws NullPointerException if request or any required field is null
     */
    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = Ticket.builder()
                .ticketId(UUID.randomUUID())
                .subject(request.subject())
                .description(request.description())
                .status(TicketStatus.OPEN) // All new tickets start as OPEN
                .userId(request.userId())
                .assigneeId("agent-123") // Adding a default hardcoded agent here just for testing (No agent-assigning api at the moment)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return repository.save(ticket);
    }

    /**
     * Retrieves a filtered list of tickets based on the provided criteria.
     * 
     * <p>This method supports filtering by multiple criteria simultaneously.
     * All filters are optional - if a filter
     * parameter is null, that filter is not applied. The filtering is performed
     * in-memory using Java Streams.</p>
     * 
     * <p>Supported filters:</p>
     * <ul>
     *   <li>status: Filter by ticket status (open, in_progress, resolved, closed - case-insensitive)</li>
     *   <li>userId: Filter by the ticket creator (for customer filtering)</li>
     *   <li>assigneeId: Filter by the assigned support agent (for agent filtering)</li>
     * </ul>
     * 
     * <p>Business Rule Implementation:</p>
     * <ul>
     *   <li><strong>"Only public comments are visible to users"</strong></li>
     *   <li>When userId is specified: Customer request → Only public comments visible</li>
     *   <li>When assigneeId is specified: Agent request → All comments visible</li>
     *   <li>When neither specified: All comments visible (general listing)</li>
     * </ul>
     * 
     * <p>Example usage:</p>
     * <ul>
     *   <li>GET /tickets?status=open&amp;userId=user-001 (customer - only public comments)</li>
     *   <li>GET /tickets?assigneeId=agent-123 (agent - all comments)</li>
     *   <li>GET /tickets (general listing - all comments)</li>
     * </ul>
     * 
     * @param status optional status filter (open, in_progress, resolved, closed - case-insensitive), null to ignore
     * @param userId optional user ID filter (for customer), null to ignore - triggers public-only comment filtering
     * @param assigneeId optional assignee ID filter (for agent), null to ignore - shows all comments
     * @return a list of tickets matching the specified criteria with appropriate comment filtering
     */
    public List<Ticket> listTickets(String status, String userId, String assigneeId) {
        return repository.findAll().stream()
                // Filter by status if provided (case-insensitive comparison)
                .filter(t -> status == null || t.getStatus().name().equalsIgnoreCase(status))
                // Filter by user ID if provided (exact match)
                .filter(t -> userId == null || t.getUserId().equals(userId))
                // Filter by assignee ID if provided (exact match, handles null assigneeId)
                .filter(t -> assigneeId == null || assigneeId.equals(t.getAssigneeId()))
                // Apply comment visibility filtering based on request type
                .map(ticket -> filterCommentsBasedOnRequestType(ticket, userId))
                .toList();
    }

    /**
     * Filters comments in a ticket based on the request type (user vs agent).
     * 
     * <p>This method implements the business rule:
     * <strong>"Only public comments are visible to users"</strong></p>
     * 
     * <p>Filtering logic based on existing parameters:</p>
     * <ul>
     *   <li>userId specified: Customer request → Returns ticket with only PUBLIC comments</li>
     *   <li>assigneeId specified: Agent request → Returns ticket with ALL comments</li>
     *   <li>Neither specified: General listing → Returns ticket with ALL comments</li>
     * </ul>
     * 
     * @param ticket the original ticket with all comments
     * @param userId the userId filter parameter (indicates customer request if not null)
     * @return a new ticket with filtered comments based on request type
     */
    private Ticket filterCommentsBasedOnRequestType(Ticket ticket, String userId) {
        // If userId is specified, this is a customer request - show only public comments
        if (userId != null) {
            List<Comment> publicComments = ticket.getComments().stream()
                    .filter(comment -> comment.visibility() == CommentVisibility.PUBLIC)
                    .toList();
            
            // Create a new ticket with filtered comments
            return Ticket.builder()
                    .ticketId(ticket.getTicketId())
                    .subject(ticket.getSubject())
                    .description(ticket.getDescription())
                    .status(ticket.getStatus())
                    .userId(ticket.getUserId())
                    .assigneeId(ticket.getAssigneeId())
                    .createdAt(ticket.getCreatedAt())
                    .updatedAt(ticket.getUpdatedAt())
                    .comments(publicComments)
                    .build();
        }
        
        // If assigneeId is specified or neither is specified, show all comments
        // (agent request or general listing)
        return ticket;
    }

    /**
     * Updates the status of an existing ticket.
     * 
     * <p>This method enforces business rules around status transitions:</p>
     * <ul>
     *   <li>Prevents invalid status transitions (e.g., cannot move from closed to in_progress)</li>
     *   <li>Status values are validated against the TicketStatus enum</li>
     *   <li>The updatedAt timestamp is automatically set</li>
     * </ul>
     * 
     * <p>Expected request format:</p>
     * <pre>
     * {
     *   "status": "in_progress"
     * }
     * </pre>
     * 
     * <p>Valid status values: open, in_progress, resolved, closed</p>
     * 
     * @param ticketId the UUID of the ticket to update
     * @param newStatus the new status value (case-insensitive)
     * @return the updated ticket
     * @throws NoSuchElementException if no ticket exists with the given ID
     * @throws IllegalArgumentException if the newStatus is not a valid TicketStatus
     * @throws IllegalStateException if attempting to update a closed ticket (business rule violation)
     */
    public Ticket updateStatus(UUID ticketId, String newStatus) {
        // Retrieve the ticket or throw exception if not found
        Ticket ticket = repository.findById(ticketId).orElseThrow();
        
        TicketStatus current = ticket.getStatus();
        TicketStatus updated = TicketStatus.valueOf(newStatus.toUpperCase());
        
        // Business rule: Cannot update closed tickets
        if (current == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot update closed ticket");
        }
        
        // Update the ticket status and timestamp
        ticket.setStatus(updated);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(ticket);
    }

    /**
     * Adds a new comment to an existing ticket.
     * 
     * <p>This method creates a new comment and associates it with the specified
     * ticket. The comment visibility system supports both public and internal
     * comments:</p>
     * <ul>
     *   <li>Only public comments are visible to users</li>
     *   <li>Agents can add both public and internal comments</li>
     *   <li>Comments are immutable once created for audit trail</li>
     * </ul>
     * 
     * <p>Expected request format:</p>
     * <pre>
     * {
     *   "authorId": "agent-123",
     *   "content": "We're currently investigating your issue.",
     *   "visibility": "public"
     * }
     * </pre>
     * 
     * <p>Comment processing:</p>
     * <ul>
     *   <li>Generates a unique comment ID (UUID)</li>
     *   <li>Sets creation timestamp</li>
     *   <li>Validates and sets visibility level (public/internal)</li>
     *   <li>Adds comment to ticket's comment list</li>
     *   <li>Updates ticket's updatedAt timestamp</li>
     * </ul>
     * 
     * @param ticketId the UUID of the ticket to add the comment to
     * @param request the comment request containing author, content, and visibility
     * @return the updated ticket with the new comment added
     * @throws NoSuchElementException if no ticket exists with the given ID
     * @throws IllegalArgumentException if the visibility value is not valid (public/internal)
     */
    public Ticket addComment(UUID ticketId, AddCommentRequest request) {
        // Retrieve the ticket or throw exception if not found
        Ticket ticket = repository.findById(ticketId).orElseThrow();
        
        // Create the new comment with generated ID and timestamp
        Comment comment = new Comment(
                UUID.randomUUID(),
                ticketId,
                request.authorId(),
                request.content(),
                CommentVisibility.valueOf(request.visibility().toUpperCase()),
                LocalDateTime.now()
        );
        
        // Add comment to ticket and update timestamp
        ticket.getComments().add(comment);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(ticket);
    }
}
