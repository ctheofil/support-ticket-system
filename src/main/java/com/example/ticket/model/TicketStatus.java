package com.example.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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
    CLOSED;

    /**
     * Returns the lowercase string representation of the enum value with underscores.
     * This is used for JSON serialization.
     * 
     * @return lowercase string representation (e.g., "in_progress")
     */
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    /**
     * Creates a TicketStatus enum from a string value (case-insensitive).
     * This method is used by Jackson for JSON deserialization.
     * 
     * @param value the string value to convert
     * @return the corresponding TicketStatus enum
     * @throws IllegalArgumentException if the value is not a valid status option
     */
    @JsonCreator
    public static TicketStatus fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty. Valid values are: " + getValidValues());
        }
        
        String normalizedValue = value.trim().toUpperCase();
        
        try {
            return TicketStatus.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid status value '%s'. Valid values are: %s", value, getValidValues())
            );
        }
    }

    /**
     * Returns a comma-separated string of all valid status values.
     * 
     * @return string containing all valid values
     */
    private static String getValidValues() {
        return Arrays.stream(TicketStatus.values())
                .map(TicketStatus::toValue)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
