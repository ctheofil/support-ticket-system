package com.example.ticket.service;

import com.example.ticket.dto.CreateTicketRequest;
import com.example.ticket.model.Ticket;
import com.example.ticket.model.TicketStatus;
import com.example.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests focused specifically on ticket creation functionality.
 * 
 * This test class covers all aspects of ticket creation including:
 * - Field validation and assignment
 * - ID generation and uniqueness
 * - Timestamp handling
 * - Initial status assignment
 * - Default values
 */
@DisplayName("Ticket Creation Service Tests")
class TicketCreationServiceTest {

    private TicketService ticketService;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketRepository = new TicketRepository();
        ticketService = new TicketService(ticketRepository);
    }

    @Test
    @DisplayName("Should create ticket with all required fields")
    void shouldCreateTicketWithAllRequiredFields() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(
            "user-001", 
            "Payment issue", 
            "I was charged twice for the same order."
        );

        // When
        Ticket ticket = ticketService.createTicket(request);

        // Then
        assertNotNull(ticket.getTicketId(), "Ticket ID should be generated");
        assertEquals("Payment issue", ticket.getSubject());
        assertEquals("I was charged twice for the same order.", ticket.getDescription());
        assertEquals(TicketStatus.OPEN, ticket.getStatus(), "New tickets should have OPEN status");
        assertEquals("user-001", ticket.getUserId());
        assertNotNull(ticket.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(ticket.getUpdatedAt(), "Updated timestamp should be set");
        assertTrue(ticket.getComments().isEmpty(), "New tickets should have no comments");
    }

    @Test
    @DisplayName("Should create ticket with unique ID for each request")
    void shouldCreateTicketWithUniqueIdForEachRequest() {
        // Given
        CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Subject 1", "Description 1");
        CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Subject 2", "Description 2");

        // When
        Ticket ticket1 = ticketService.createTicket(request1);
        Ticket ticket2 = ticketService.createTicket(request2);

        // Then
        assertNotEquals(ticket1.getTicketId(), ticket2.getTicketId(), "Each ticket should have unique ID");
    }

    @Test
    @DisplayName("Should set creation and update timestamps to same value for new ticket")
    void shouldSetTimestampsCorrectlyForNewTicket() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("user-001", "Test", "Test description");

        // When
        Ticket ticket = ticketService.createTicket(request);

        // Then
        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getUpdatedAt());
        // Timestamps should be very close (within 1 second)
        assertTrue(ticket.getCreatedAt().isBefore(ticket.getUpdatedAt().plusSeconds(1)));
        assertTrue(ticket.getCreatedAt().isAfter(ticket.getUpdatedAt().minusSeconds(1)));
    }

    @Test
    @DisplayName("Should create multiple tickets with different user IDs")
    void shouldCreateMultipleTicketsWithDifferentUserIds() {
        // Given
        CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Issue 1", "Description 1");
        CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Issue 2", "Description 2");
        CreateTicketRequest request3 = new CreateTicketRequest("user-001", "Issue 3", "Description 3");

        // When
        Ticket ticket1 = ticketService.createTicket(request1);
        Ticket ticket2 = ticketService.createTicket(request2);
        Ticket ticket3 = ticketService.createTicket(request3);

        // Then
        assertEquals("user-001", ticket1.getUserId());
        assertEquals("user-002", ticket2.getUserId());
        assertEquals("user-001", ticket3.getUserId());
        
        // All should have unique IDs
        assertNotEquals(ticket1.getTicketId(), ticket2.getTicketId());
        assertNotEquals(ticket1.getTicketId(), ticket3.getTicketId());
        assertNotEquals(ticket2.getTicketId(), ticket3.getTicketId());
    }

    @Test
    @DisplayName("Should persist created ticket in repository")
    void shouldPersistCreatedTicketInRepository() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Test description");

        // When
        Ticket createdTicket = ticketService.createTicket(request);

        // Then
        Ticket retrievedTicket = ticketRepository.findById(createdTicket.getTicketId()).orElse(null);
        assertNotNull(retrievedTicket, "Ticket should be persisted in repository");
        assertEquals(createdTicket.getTicketId(), retrievedTicket.getTicketId());
        assertEquals(createdTicket.getSubject(), retrievedTicket.getSubject());
        assertEquals(createdTicket.getDescription(), retrievedTicket.getDescription());
    }

    @Test
    @DisplayName("Should handle various subject and description lengths")
    void shouldHandleVariousSubjectAndDescriptionLengths() {
        // Given
        CreateTicketRequest shortRequest = new CreateTicketRequest("user-001", "Bug", "Fix it");
        CreateTicketRequest longRequest = new CreateTicketRequest("user-002", 
            "Very long subject that describes a complex issue with multiple components and systems involved",
            "This is a very detailed description that explains the issue in great detail, including steps to reproduce, expected behavior, actual behavior, environment details, and any other relevant information that might help in resolving the issue."
        );

        // When
        Ticket shortTicket = ticketService.createTicket(shortRequest);
        Ticket longTicket = ticketService.createTicket(longRequest);

        // Then
        assertEquals("Bug", shortTicket.getSubject());
        assertEquals("Fix it", shortTicket.getDescription());
        assertTrue(longTicket.getSubject().length() > 50);
        assertTrue(longTicket.getDescription().length() > 200);
        assertEquals(TicketStatus.OPEN, shortTicket.getStatus());
        assertEquals(TicketStatus.OPEN, longTicket.getStatus());
    }
}
