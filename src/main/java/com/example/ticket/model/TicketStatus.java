
package com.example.ticket.model;

/**
 * Enumeration representing the possible states of a support ticket.
 * 
 * <p>This enum defines the lifecycle of a ticket from creation to closure.
 * Business logic enforces the status transitions to ensure proper
 * workflow management.</p>
 * 
 * <p>Valid status transitions:</p>
 * <ul>
 *   <li>OPEN → IN_PROGRESS (when agent starts working)</li>
 *   <li>IN_PROGRESS → RESOLVED (when issue is fixed)</li>
 *   <li>RESOLVED → CLOSED (when user confirms resolution)</li>
 *   <li>Any status → CLOSED (administrative closure)</li>
 * </ul>
 * 
 * <p>Note: Once a ticket is CLOSED, no further status changes are allowed.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
public enum TicketStatus {
    
    /**
     * Initial status when a ticket is first created.
     * Indicates the ticket is waiting to be picked up by a support agent.
     */
    OPEN,
    
    /**
     * Status indicating a support agent is actively working on the ticket.
     * Used to track tickets currently being investigated or resolved.
     */
    IN_PROGRESS,
    
    /**
     * Status indicating the issue has been resolved by the support team.
     * Waiting for user confirmation or automatic closure after a period.
     */
    RESOLVED,
    
    /**
     * Final status indicating the ticket is completely closed.
     * No further modifications are allowed once a ticket reaches this state.
     */
    CLOSED
}
