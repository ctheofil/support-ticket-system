
package com.example.ticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for adding comments to existing tickets.
 * 
 * <p>This DTO encapsulates the information needed to add a new comment
 * to a ticket. Comments can be either
 * public (visible to customers) or internal (visible only to support staff).</p>
 * 
 * <p>The comment will be automatically assigned a unique ID and timestamp
 * when processed. All fields are required for successful comment creation.</p>
 * 
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only public comments are visible to users</li>
 *   <li>Agents can add both public and internal comments</li>
 *   <li>Comments are immutable once created for audit trail</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {
 *   "authorId": "agent-123",
 *   "content": "We're currently investigating your issue.",
 *   "visibility": "public"
 * }
 * </pre>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 * 
 * @param authorId Identifier of the user adding the comment.
 *                 Can be either a customer or support agent. Used for tracking
 *                 who made each comment and for access control. Must not be null or empty.
 * @param content The text content of the comment.
 *                Contains the actual message, note, or update information
 *                to be added to the ticket. Must not be null or empty.
 * @param visibility Visibility level for this comment.
 *                   Must be either "public" or "internal" (case-insensitive).
 *                   <p>Visibility options:</p>
 *                   <ul>
 *                     <li>public: Visible to both users and agents</li>
 *                     <li>internal: Visible only to support staff</li>
 *                   </ul>
 * @see com.example.ticket.model.CommentVisibility
 */
public record AddCommentRequest(
    @NotBlank(message = "Author ID is required and cannot be empty")
    String authorId,
    
    @NotBlank(message = "Content is required and cannot be empty")
    String content,
    
    String visibility
) {}
