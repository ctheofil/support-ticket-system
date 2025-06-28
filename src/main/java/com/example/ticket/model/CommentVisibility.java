
package com.example.ticket.model;

/**
 * Enumeration representing the visibility levels for ticket comments.
 * 
 * <p>This enum controls who can view specific comments on a ticket.
 * It enables support teams to maintain internal notes while also
 * providing transparent communication with customers.</p>
 * 
 * <p>Visibility rules:</p>
 * <ul>
 *   <li>PUBLIC: Visible to both customers and support staff</li>
 *   <li>INTERNAL: Visible only to support staff and administrators</li>
 * </ul>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
public enum CommentVisibility {
    
    /**
     * Comment is visible to all parties including the ticket creator.
     * Used for customer communication and status updates.
     */
    PUBLIC,
    
    /**
     * Comment is visible only to support staff and administrators.
     * Used for internal notes, troubleshooting steps, and private discussions.
     */
    INTERNAL
}
