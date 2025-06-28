
package com.example.ticket.dto;

/**
 * Data Transfer Object for updating the status of an existing ticket.
 * 
 * <p>This DTO is used to change the status of a ticket through the REST API
 * The status value must be a valid TicketStatus
 * enum value (case-insensitive). The system will validate status transitions
 * to ensure proper workflow and prevent invalid state changes.</p>
 * 
 * <p>Valid status values:</p>
 * <ul>
 *   <li>open - Initial status when ticket is created</li>
 *   <li>in_progress - Agent is actively working on the ticket</li>
 *   <li>resolved - Issue has been resolved</li>
 *   <li>closed - Ticket is completely closed</li>
 * </ul>
 * 
 * <p>Business rules:</p>
 * <ul>
 *   <li>Closed tickets cannot have their status updated</li>
 *   <li>Invalid status transitions are prevented</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {
 *   "status": "in_progress"
 * }
 * </pre>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 * 
 * @param status The new status to set for the ticket.
 *               Must be a valid TicketStatus enum value (open, in_progress, resolved, closed).
 *               The value is case-insensitive and will be converted to uppercase during processing.
 * @see com.example.ticket.model.TicketStatus
 */
public record UpdateStatusRequest(String status) {}
