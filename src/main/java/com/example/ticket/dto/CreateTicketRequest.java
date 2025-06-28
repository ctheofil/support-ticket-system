
package com.example.ticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for creating new support tickets.
 * 
 * <p>This DTO encapsulates the minimum required information needed to create
 * a new ticket in the system. The ticket will be automatically assigned a
 * unique ID, timestamps, and initial status (OPEN) when processed.</p>
 * 
 * <p>All fields are required for successful ticket creation. The system
 * will validate that none of the fields are null or empty before processing.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 * 
 * @param userId Identifier of the user creating the ticket.
 *               Used to associate the ticket with the requesting user for tracking
 *               and access control purposes. Must not be null or empty.
 * @param subject Brief summary or title of the support request.
 *                Should be concise but descriptive enough to understand the nature
 *                of the issue or request. Must not be null or empty.
 * @param description Detailed description of the issue or request.
 *                    Should include all relevant information needed for support staff
 *                    to understand and address the problem. Must not be null or empty.
 */
public record CreateTicketRequest(
    @NotBlank(message = "User ID is required and cannot be empty")
    String userId,
    
    @NotBlank(message = "Subject is required and cannot be empty")
    String subject,
    
    @NotBlank(message = "Description is required and cannot be empty")
    String description
) {}
