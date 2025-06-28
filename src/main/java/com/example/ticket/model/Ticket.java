
package com.example.ticket.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a support ticket in the system.
 * 
 * <p>A ticket is the core entity that tracks customer support requests.
 * Each ticket has a unique identifier, subject, description, status, and
 * associated metadata including creation/update timestamps and comments.</p>
 * 
 * <p>Tickets follow a defined lifecycle through various statuses:
 * OPEN → IN_PROGRESS → RESOLVED → CLOSED</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    /**
     * Unique identifier for the ticket.
     * Generated automatically when a ticket is created.
     */
    private UUID ticketId;
    
    /**
     * Brief summary or title of the support request.
     * Should be descriptive enough to understand the issue at a glance.
     */
    private String subject;
    
    /**
     * Detailed description of the support request or issue.
     * Contains the full context and details provided by the user.
     */
    private String description;
    
    /**
     * Current status of the ticket in its lifecycle.
     * Determines what actions can be performed on the ticket.
     * 
     * @see TicketStatus
     */
    private TicketStatus status;
    
    /**
     * Identifier of the user who created the ticket.
     * Used for filtering and access control.
     */
    private String userId;
    
    /**
     * Identifier of the support agent assigned to handle this ticket.
     * Can be null if the ticket hasn't been assigned yet.
     */
    private String assigneeId;
    
    /**
     * Timestamp when the ticket was initially created.
     * Set automatically during ticket creation and never modified.
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the ticket was last modified.
     * Updated whenever the ticket status changes or comments are added.
     */
    private LocalDateTime updatedAt;
    
    /**
     * List of comments associated with this ticket.
     * Includes both public comments (visible to users) and internal notes.
     * Initialized as an empty ArrayList to prevent null pointer exceptions.
     * 
     * @see Comment
     */
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}
