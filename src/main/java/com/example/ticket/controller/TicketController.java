
package com.example.ticket.controller;

import com.example.ticket.dto.*;
import com.example.ticket.model.Ticket;
import com.example.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

/**
 * REST controller for managing support tickets through HTTP endpoints.
 * 
 * <p>This controller provides a RESTful API for the support ticket system,
 * exposing endpoints for ticket creation, retrieval, status updates, and
 * comment management. All endpoints return JSON responses and accept JSON
 * request bodies where applicable.</p>
 * 
 * <p>Available endpoints:</p>
 * <ul>
 *   <li>POST /tickets - Create a new ticket</li>
 *   <li>GET /tickets - List tickets with optional filtering</li>
 *   <li>PATCH /tickets/{id}/status - Update ticket status</li>
 *   <li>POST /tickets/{id}/comments - Add comment to ticket</li>
 * </ul>
 * 
 * <p>Error Handling: The controller relies on Spring Boot's default error
 * handling for common exceptions like NoSuchElementException and
 * IllegalArgumentException, which are automatically converted to appropriate
 * HTTP status codes.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    /**
     * Service layer dependency for ticket business logic.
     * Injected via constructor using Lombok's @RequiredArgsConstructor.
     */
    private final TicketService ticketService;

    /**
     * Creates a new support ticket.
     * 
     * <p>This endpoint accepts a JSON request body containing the ticket details
     * and returns the created ticket with generated ID and timestamps. The ticket
     * is automatically assigned an OPEN status.</p>
     *
     * <p>Example request body:</p>
     * <pre>
     * {
     *   "userId": "user-001",
     *   "subject": "Payment issue",
     *   "description": "I was charged twice for the same order."
     * }
     * </pre>
     * 
     * @param request the ticket creation request containing user ID, subject, and description
     * @return the newly created ticket with generated ID, status, and timestamps
     * @throws IllegalArgumentException if request validation fails
     */
    @PostMapping
    public Ticket createTicket(@RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    /**
     * Retrieves a list of tickets with optional filtering.
     * 
     * <p>This endpoint supports filtering tickets by status, user ID, and assignee ID.
     * All filter parameters are optional - if not provided, all tickets are returned.
     * Multiple filters can be applied simultaneously.</p>
     * 
     * <p>Query parameters:</p>
     * <ul>
     *   <li>status: Filter by ticket status (open, in_progress, resolved, closed)</li>
     *   <li>userId: Filter by the user who created the ticket (for customer)</li>
     *   <li>assigneeId: Filter by the assigned support agent (for agent)</li>
     * </ul>
     *
     * <p>Example usage:</p>
     * <ul>
     *   <li>GET /tickets - Returns all tickets</li>
     *   <li>GET /tickets?status=open - Returns only open tickets</li>
     *   <li>GET /tickets?status=open&amp;userId=user-001 - Returns open tickets for user-001</li>
     *   <li>GET /tickets?userId=user-001&amp;assigneeId=agent-123 - Returns tickets for specific user and agent</li>
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
     * @param status optional status filter (open, in_progress, resolved, closed - case-insensitive)
     * @param userId optional user ID filter (for customer filtering) - triggers public-only comment visibility
     * @param assigneeId optional assignee ID filter (for agent filtering) - shows all comments
     * @return a list of tickets matching the specified criteria with appropriate comment filtering
     */
    @GetMapping
    public List<Ticket> listTickets(@RequestParam(required = false) String status,
                                    @RequestParam(required = false) String userId,
                                    @RequestParam(required = false) String assigneeId) {
        return ticketService.listTickets(status, userId, assigneeId);
    }

    /**
     * Updates the status of an existing ticket.
     * 
     * <p>This endpoint allows changing the status of a ticket through the ticket
     * lifecycle. The request body should contain the new status value. Status
     * transitions are validated according to business rules.</p>
     * 
     * <p>Example request body:</p>
     * <pre>
     * {
     *   "status": "in_progress"
     * }
     * </pre>
     * 
     * <p>Valid status values: open, in_progress, resolved, closed</p>
     * 
     * <p>Business rules:</p>
     * <ul>
     *   <li>Closed tickets cannot have their status updated</li>
     *   <li>Status must be a valid TicketStatus enum value</li>
     *   <li>Status transitions are enforced to prevent invalid state changes</li>
     * </ul>
     * 
     * @param ticketId the UUID of the ticket to update
     * @param request the status update request containing the new status
     * @return the updated ticket with new status and updated timestamp
     * @throws NoSuchElementException if no ticket exists with the given ID (404)
     * @throws IllegalArgumentException if the status value is invalid (400)
     * @throws IllegalStateException if attempting to update a closed ticket (500)
     */
    @PatchMapping("/{ticketId}/status")
    public Ticket updateStatus(@PathVariable UUID ticketId, @RequestBody UpdateStatusRequest request) {
        return ticketService.updateStatus(ticketId, request.status());
    }

    /**
     * Adds a comment to an existing ticket.
     * 
     * <p>This endpoint allows adding comments to tickets for communication and
     * tracking purposes. Comments can be either public (visible to customers)
     * or internal (visible only to support staff).</p>
     * 
     * <p>Example request body:</p>
     * <pre>
     * {
     *   "authorId": "agent-123",
     *   "content": "We're currently investigating your issue.",
     *   "visibility": "public"
     * }
     * </pre>
     * 
     * <p>Visibility options:</p>
     * <ul>
     *   <li>public: Visible to both users and agents</li>
     *   <li>internal: Visible only to support staff</li>
     * </ul>
     * 
     * <p>Business rules:</p>
     * <ul>
     *   <li>Only public comments are visible to users</li>
     *   <li>Agents can add both public and internal comments</li>
     *   <li>Comments are immutable once created for audit trail</li>
     * </ul>
     * 
     * @param ticketId the UUID of the ticket to add the comment to
     * @param request the comment request containing author, content, and visibility
     * @return the updated ticket with the new comment added
     * @throws NoSuchElementException if no ticket exists with the given ID (404)
     * @throws IllegalArgumentException if the visibility value is invalid (400)
     */
    @PostMapping("/{ticketId}/comments")
    public Ticket addComment(@PathVariable UUID ticketId, @RequestBody AddCommentRequest request) {
        return ticketService.addComment(ticketId, request);
    }
}
