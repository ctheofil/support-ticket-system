
package com.example.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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
    INTERNAL;

    /**
     * Returns the lowercase string representation of the enum value.
     * This is used for JSON serialization.
     * 
     * @return lowercase string representation
     */
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    /**
     * Creates a CommentVisibility enum from a string value (case-insensitive).
     * This method is used by Jackson for JSON deserialization.
     * 
     * @param value the string value to convert
     * @return the corresponding CommentVisibility enum
     * @throws IllegalArgumentException if the value is not a valid visibility option
     */
    @JsonCreator
    public static CommentVisibility fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Visibility cannot be null or empty. Valid values are: " + getValidValues());
        }
        
        try {
            return CommentVisibility.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid visibility value '%s'. Valid values are: %s", value, getValidValues())
            );
        }
    }

    /**
     * Returns a comma-separated string of all valid visibility values.
     * 
     * @return string containing all valid values
     */
    private static String getValidValues() {
        return Arrays.stream(CommentVisibility.values())
                .map(CommentVisibility::toValue)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
