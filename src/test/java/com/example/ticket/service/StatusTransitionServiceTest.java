package com.example.ticket.service;

import com.example.ticket.dto.CreateTicketRequest;
import com.example.ticket.model.Ticket;
import com.example.ticket.model.TicketStatus;
import com.example.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests focused specifically on ticket status transition functionality.
 * 
 * This test class covers all aspects of status transitions including:
 * - Valid status transition paths
 * - Business rule enforcement (closed tickets cannot be updated)
 * - Case-insensitive status handling
 * - Error scenarios for invalid transitions
 * - Timestamp updates during transitions
 * - Field preservation during status updates
 */
@DisplayName("Status Transition Service Tests")
class StatusTransitionServiceTest {

    private TicketService ticketService;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketRepository = new TicketRepository();
        ticketService = new TicketService(ticketRepository);
    }

    @Nested
    @DisplayName("Valid Status Transitions")
    class ValidStatusTransitionTests {

        @Test
        @DisplayName("Should update status from OPEN to IN_PROGRESS")
        void shouldUpdateStatusFromOpenToInProgress() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            LocalDateTime originalUpdatedAt = ticket.getUpdatedAt();

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // Then
            assertEquals(TicketStatus.IN_PROGRESS, updatedTicket.getStatus());
            assertTrue(updatedTicket.getUpdatedAt().isAfter(originalUpdatedAt), "Updated timestamp should be newer");
        }

        @Test
        @DisplayName("Should update status from IN_PROGRESS to RESOLVED")
        void shouldUpdateStatusFromInProgressToResolved() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "resolved");

            // Then
            assertEquals(TicketStatus.RESOLVED, updatedTicket.getStatus());
        }

        @Test
        @DisplayName("Should update status from RESOLVED to CLOSED")
        void shouldUpdateStatusFromResolvedToClosed() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "resolved");

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "closed");

            // Then
            assertEquals(TicketStatus.CLOSED, updatedTicket.getStatus());
        }

        @Test
        @DisplayName("Should update status from any status to CLOSED (administrative closure)")
        void shouldUpdateStatusFromAnyStatusToClosed() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When - directly close from OPEN
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "closed");

            // Then
            assertEquals(TicketStatus.CLOSED, updatedTicket.getStatus());
        }

        @Test
        @DisplayName("Should handle complete status lifecycle")
        void shouldHandleCompleteStatusLifecycle() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Lifecycle test", "Testing full lifecycle");
            Ticket ticket = ticketService.createTicket(request);
            assertEquals(TicketStatus.OPEN, ticket.getStatus());

            // When & Then - Follow complete lifecycle
            ticket = ticketService.updateStatus(ticket.getTicketId(), "in_progress");
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

            ticket = ticketService.updateStatus(ticket.getTicketId(), "resolved");
            assertEquals(TicketStatus.RESOLVED, ticket.getStatus());

            ticket = ticketService.updateStatus(ticket.getTicketId(), "closed");
            assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        }
    }

    @Nested
    @DisplayName("Case Sensitivity Tests")
    class CaseSensitivityTests {

        @Test
        @DisplayName("Should handle case-insensitive status values")
        void shouldHandleCaseInsensitiveStatusValues() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When - test various case combinations
            Ticket updated1 = ticketService.updateStatus(ticket.getTicketId(), "IN_PROGRESS");
            Ticket updated2 = ticketService.updateStatus(ticket.getTicketId(), "resolved");
            Ticket updated3 = ticketService.updateStatus(ticket.getTicketId(), "CLOSED");

            // Then
            assertEquals(TicketStatus.CLOSED, updated3.getStatus());
        }

        @Test
        @DisplayName("Should handle mixed case status values")
        void shouldHandleMixedCaseStatusValues() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When - test mixed case
            Ticket updated = ticketService.updateStatus(ticket.getTicketId(), "In_Progress");

            // Then
            assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());
        }
    }

    @Nested
    @DisplayName("Business Rule Enforcement")
    class BusinessRuleEnforcementTests {

        @Test
        @DisplayName("Should throw IllegalStateException when updating closed ticket")
        void shouldThrowIllegalStateExceptionWhenUpdatingClosedTicket() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "closed");

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.updateStatus(ticket.getTicketId(), "in_progress"),
                "Should throw IllegalStateException when updating closed ticket"
            );
            assertEquals("Cannot update closed ticket", exception.getMessage());
        }

        @Test
        @DisplayName("Should prevent any status change on closed tickets")
        void shouldPreventAnyStatusChangeOnClosedTickets() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "closed");

            // When & Then - Try all possible status changes
            assertThrows(IllegalStateException.class, 
                () -> ticketService.updateStatus(ticket.getTicketId(), "open"));
            assertThrows(IllegalStateException.class, 
                () -> ticketService.updateStatus(ticket.getTicketId(), "in_progress"));
            assertThrows(IllegalStateException.class, 
                () -> ticketService.updateStatus(ticket.getTicketId(), "resolved"));
            assertThrows(IllegalStateException.class, 
                () -> ticketService.updateStatus(ticket.getTicketId(), "closed")); // Even to same status
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw NoSuchElementException for non-existent ticket")
        void shouldThrowNoSuchElementExceptionForNonExistentTicket() {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            assertThrows(
                NoSuchElementException.class,
                () -> ticketService.updateStatus(nonExistentId, "in_progress"),
                "Should throw NoSuchElementException for non-existent ticket"
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid status")
        void shouldThrowIllegalArgumentExceptionForInvalidStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateStatus(ticket.getTicketId(), "invalid_status"),
                "Should throw IllegalArgumentException for invalid status"
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null status")
        void shouldThrowIllegalArgumentExceptionForNullStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateStatus(ticket.getTicketId(), null),
                "Should throw IllegalArgumentException for null status"
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty status")
        void shouldThrowIllegalArgumentExceptionForEmptyStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateStatus(ticket.getTicketId(), ""),
                "Should throw IllegalArgumentException for empty status"
            );
        }
    }

    @Nested
    @DisplayName("Field Preservation Tests")
    class FieldPreservationTests {

        @Test
        @DisplayName("Should preserve all other ticket fields when updating status")
        void shouldPreserveAllOtherTicketFieldsWhenUpdatingStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket originalTicket = ticketService.createTicket(request);

            // Wait a small amount to ensure timestamp difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            Ticket updatedTicket = ticketService.updateStatus(originalTicket.getTicketId(), "in_progress");

            // Then
            assertEquals(originalTicket.getTicketId(), updatedTicket.getTicketId());
            assertEquals(originalTicket.getSubject(), updatedTicket.getSubject());
            assertEquals(originalTicket.getDescription(), updatedTicket.getDescription());
            assertEquals(originalTicket.getUserId(), updatedTicket.getUserId());
            assertEquals(originalTicket.getAssigneeId(), updatedTicket.getAssigneeId());
            assertEquals(originalTicket.getCreatedAt(), updatedTicket.getCreatedAt());
            assertEquals(originalTicket.getComments(), updatedTicket.getComments());
            // Only status and updatedAt should change
            assertEquals(TicketStatus.IN_PROGRESS, updatedTicket.getStatus()); // Status should be updated
            assertNotEquals(TicketStatus.OPEN, updatedTicket.getStatus()); // Should not be the original status
            assertTrue(updatedTicket.getUpdatedAt().isAfter(originalTicket.getUpdatedAt()) || 
                      updatedTicket.getUpdatedAt().equals(originalTicket.getUpdatedAt()),
                      "Updated timestamp should be newer or equal");
        }

        @Test
        @DisplayName("Should preserve comments when updating status")
        void shouldPreserveCommentsWhenUpdatingStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            
            // Add a comment before status update
            ticketService.addComment(ticket.getTicketId(), 
                new com.example.ticket.dto.AddCommentRequest("agent-123", "Test comment", "public"));

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // Then
            assertEquals(1, updatedTicket.getComments().size(), "Comments should be preserved");
            assertEquals("Test comment", updatedTicket.getComments().get(0).content());
        }
    }

    @Nested
    @DisplayName("Timestamp Management Tests")
    class TimestampManagementTests {

        @Test
        @DisplayName("Should update timestamp when status changes")
        void shouldUpdateTimestampWhenStatusChanges() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            LocalDateTime originalUpdatedAt = ticket.getUpdatedAt();

            // Wait a small amount to ensure timestamp difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // Then
            assertTrue(updatedTicket.getUpdatedAt().isAfter(originalUpdatedAt), 
                "Updated timestamp should be newer than original");
        }

        @Test
        @DisplayName("Should not change created timestamp when updating status")
        void shouldNotChangeCreatedTimestampWhenUpdatingStatus() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            LocalDateTime originalCreatedAt = ticket.getCreatedAt();

            // When
            Ticket updatedTicket = ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // Then
            assertEquals(originalCreatedAt, updatedTicket.getCreatedAt(), 
                "Created timestamp should never change");
        }
    }
}
