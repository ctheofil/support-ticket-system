package com.example.ticket.integration;

import com.example.ticket.dto.CreateTicketRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 500 when trying to update closed ticket")
    void testBusinessRuleViolationReturns500() throws Exception {
        // Given - Create and close a ticket
        CreateTicketRequest request = new CreateTicketRequest("test-user", "Test Ticket", "Test Description");
        
        String response = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

        // Close the ticket
        String closeRequest = """
            { "status": "closed" }
            """;

        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(closeRequest))
                .andExpect(status().isOk());

        // When - Try to update the closed ticket (should trigger IllegalStateException)
        String updateRequest = """
            { "status": "in_progress" }
            """;

        // Then - Should return 500 with proper error structure
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Cannot update closed ticket"));
    }

    @Test
    @DisplayName("Should return 400 when providing invalid status value")
    void testInvalidArgumentReturns400() throws Exception {
        // Given - Create a ticket
        CreateTicketRequest request = new CreateTicketRequest("test-user", "Test Ticket", "Test Description");
        
        String response = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

        // When - Try to update with invalid status (should trigger IllegalArgumentException)
        String invalidStatusRequest = """
            { "status": "invalid_status" }
            """;

        // Then - Should return 400 with proper error structure
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidStatusRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 when trying to access non-existent ticket")
    void testResourceNotFoundReturns404() throws Exception {
        // Given - A non-existent ticket ID
        UUID nonExistentTicketId = UUID.randomUUID();

        // When - Try to update non-existent ticket (should trigger NoSuchElementException)
        String updateRequest = """
            { "status": "in_progress" }
            """;

        // Then - Should return 404 with proper error structure
        mockMvc.perform(patch("/tickets/" + nonExistentTicketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("The requested resource was not found"));
    }

    @Test
    @DisplayName("Should return 404 when adding comment to non-existent ticket")
    void testAddCommentToNonExistentTicketReturns404() throws Exception {
        // Given - A non-existent ticket ID
        UUID nonExistentTicketId = UUID.randomUUID();

        // When - Try to add comment to non-existent ticket
        String commentRequest = """
            {
              "authorId": "agent-123",
              "content": "Test comment",
              "visibility": "public"
            }
            """;

        // Then - Should return 404 with proper error structure
        mockMvc.perform(post("/tickets/" + nonExistentTicketId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("The requested resource was not found"));
    }

    @Test
    @DisplayName("Should return 400 when providing invalid comment visibility")
    void testInvalidCommentVisibilityReturns400() throws Exception {
        // Given - Create a ticket
        CreateTicketRequest request = new CreateTicketRequest("test-user", "Test Ticket", "Test Description");
        
        String response = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

        // When - Try to add comment with invalid visibility
        String invalidCommentRequest = """
            {
              "authorId": "agent-123",
              "content": "Test comment",
              "visibility": "invalid_visibility"
            }
            """;

        // Then - Should return 400 with proper error structure
        mockMvc.perform(post("/tickets/" + ticketId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCommentRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @DisplayName("Should return consistent error response structure")
    void testConsistentErrorResponseStructure() throws Exception {
        // Test multiple error scenarios to ensure consistent structure
        UUID nonExistentId = UUID.randomUUID();

        // Test 404 error structure
        mockMvc.perform(patch("/tickets/" + nonExistentId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"open\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.message").isString());

        // Test 400 error structure with existing ticket but invalid status
        CreateTicketRequest request = new CreateTicketRequest("test-user", "Test Ticket", "Test Description");
        
        String response = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"invalid\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.message").isString());
    }
}
