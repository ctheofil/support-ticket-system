
package com.example.ticket.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a comment or note attached to a support ticket.
 * 
 * <p>Comments provide a way to track communication and progress on tickets.
 * They can be either public (visible to the ticket creator) or internal
 * (visible only to support staff). Each comment is immutable once created
 * and includes metadata about when and by whom it was added.</p>
 * 
 * <p>Comments are automatically timestamped and linked to their parent ticket
 * through the ticketId field. The visibility setting determines who can
 * view the comment content.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 * 
 * @param commentId Unique identifier for this comment.
 *                  Generated automatically when the comment is created.
 * @param ticketId Identifier of the ticket this comment belongs to.
 *                 Used to maintain the relationship between comments and tickets.
 * @param authorId Identifier of the user who authored this comment.
 *                 Can be either a customer or a support agent.
 * @param content The actual text content of the comment.
 *                Contains the message, note, or update information.
 * @param visibility Visibility level of this comment.
 *                   Determines whether the comment is visible to customers or internal only.
 * @param createdAt Timestamp when this comment was created.
 *                  Set automatically when the comment is added and never modified.
 * @see CommentVisibility
 */
public record Comment(
    UUID commentId,
    UUID ticketId,
    String authorId,
    String content,
    CommentVisibility visibility,
    LocalDateTime createdAt
) {}
