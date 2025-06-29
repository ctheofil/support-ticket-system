package com.example.ticket.service;

import com.example.ticket.dto.AddCommentRequest;
import com.example.ticket.dto.CreateTicketRequest;
import com.example.ticket.model.Comment;
import com.example.ticket.model.CommentVisibility;
import com.example.ticket.model.Ticket;
import com.example.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests focused specifically on comment visibility functionality.
 * This test class covers all aspects of comment visibility including
 * - Public vs internal comment creation
 * - Visibility filtering based on request type (customer vs agent)
 * - Business rule: "Only public comments are visible to users"
 * - Case-insensitive visibility handling
 * - Error scenarios for invalid visibility values
 */
@DisplayName("Comment Visibility Service Tests")
class CommentVisibilityServiceTest {

    private TicketService ticketService;

  @BeforeEach
    void setup() {
    TicketRepository ticketRepository = new TicketRepository();
        ticketService = new TicketService(ticketRepository);
    }

    @Nested
    @DisplayName("Comment Creation Tests")
    class CommentCreationTests {

        @Test
        @DisplayName("Should create public comment successfully")
        void shouldCreatePublicCommentSuccessfully() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            AddCommentRequest commentRequest = new AddCommentRequest("agent-123", "Public comment", "public");

            // When
            Ticket updatedTicket = ticketService.addComment(ticket.getTicketId(), commentRequest);

            // Then
            assertEquals(1, updatedTicket.getComments().size(), "Should have one comment");
            Comment comment = updatedTicket.getComments().get(0);
            assertEquals(CommentVisibility.PUBLIC, comment.visibility());
            assertEquals("Public comment", comment.content());
        }

        @Test
        @DisplayName("Should create internal comment successfully")
        void shouldCreateInternalCommentSuccessfully() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            AddCommentRequest commentRequest = new AddCommentRequest("agent-456", "Internal note", "internal");

            // When
            Ticket updatedTicket = ticketService.addComment(ticket.getTicketId(), commentRequest);

            // Then
            assertEquals(1, updatedTicket.getComments().size());
            Comment comment = updatedTicket.getComments().get(0);
            assertEquals(CommentVisibility.INTERNAL, comment.visibility());
            assertEquals("Internal note", comment.content());
        }

        @Test
        @DisplayName("Should handle case-insensitive visibility values")
        void shouldHandleCaseInsensitiveVisibilityValues() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);

            // When - test various case combinations
            AddCommentRequest publicComment = new AddCommentRequest("agent-123", "Public", "PUBLIC");
            AddCommentRequest internalComment = new AddCommentRequest("agent-123", "Internal", "internal");
            
            ticketService.addComment(ticket.getTicketId(), publicComment);
            Ticket updatedTicket = ticketService.addComment(ticket.getTicketId(), internalComment);

            // Then
            assertEquals(2, updatedTicket.getComments().size());
            assertEquals(CommentVisibility.PUBLIC, updatedTicket.getComments().get(0).visibility());
            assertEquals(CommentVisibility.INTERNAL, updatedTicket.getComments().get(1).visibility());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid visibility")
        void shouldThrowIllegalArgumentExceptionForInvalidVisibility() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            AddCommentRequest commentRequest = new AddCommentRequest("agent-123", "Test comment", "invalid_visibility");

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.addComment(ticket.getTicketId(), commentRequest),
                "Should throw IllegalArgumentException for invalid visibility"
            );
        }
    }

    @Nested
    @DisplayName("Visibility Filtering Tests")
    class VisibilityFilteringTests {

        @Test
        @DisplayName("Should show only public comments when filtering by userId (customer view)")
        void shouldShowOnlyPublicCommentsWhenFilteringByUserId() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            
            // Add both public and internal comments
            AddCommentRequest publicComment = new AddCommentRequest("agent-123", "Public comment", "public");
            AddCommentRequest internalComment = new AddCommentRequest("agent-123", "Internal comment", "internal");
            ticketService.addComment(ticket.getTicketId(), publicComment);
            ticketService.addComment(ticket.getTicketId(), internalComment);

            // When - customer view (userId specified)
            List<Ticket> customerTickets = ticketService.listTickets(null, "user-001", null);

            // Then
            assertEquals(1, customerTickets.size());
            Ticket customerTicket = customerTickets.get(0);
            assertEquals(1, customerTicket.getComments().size(), "Should show only public comments");
            assertEquals(CommentVisibility.PUBLIC, customerTicket.getComments().get(0).visibility());
            assertEquals("Public comment", customerTicket.getComments().get(0).content());
        }

        @Test
        @DisplayName("Should show all comments when filtering by assigneeId (agent view)")
        void shouldShowAllCommentsWhenFilteringByAssigneeId() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticket.setAssigneeId("agent-123");
            // Add both public and internal comments
            AddCommentRequest publicComment = new AddCommentRequest("agent-123", "Public comment", "public");
            AddCommentRequest internalComment = new AddCommentRequest("agent-123", "Internal comment", "internal");
            ticketService.addComment(ticket.getTicketId(), publicComment);
            ticketService.addComment(ticket.getTicketId(), internalComment);

            // When - agent view (assigneeId specified)
            List<Ticket> agentTickets = ticketService.listTickets(null, null, "agent-123");

            // Then
            assertEquals(1, agentTickets.size());
            Ticket agentTicket = agentTickets.get(0);
            assertEquals(2, agentTicket.getComments().size(), "Should show all comments");
            
            // Verify both comment types are present
            List<CommentVisibility> visibilities = agentTicket.getComments().stream()
                .map(Comment::visibility)
                .toList();
            assertTrue(visibilities.contains(CommentVisibility.PUBLIC));
            assertTrue(visibilities.contains(CommentVisibility.INTERNAL));
        }

        @Test
        @DisplayName("Should show all comments when no user filters applied (general listing)")
        void shouldShowAllCommentsWhenNoUserFiltersApplied() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            
            // Add both public and internal comments
            AddCommentRequest publicComment = new AddCommentRequest("agent-123", "Public comment", "public");
            AddCommentRequest internalComment = new AddCommentRequest("agent-123", "Internal comment", "internal");
            ticketService.addComment(ticket.getTicketId(), publicComment);
            ticketService.addComment(ticket.getTicketId(), internalComment);

            // When - general listing (no user filters)
            List<Ticket> allTickets = ticketService.listTickets(null, null, null);

            // Then
            assertEquals(1, allTickets.size());
            Ticket generalTicket = allTickets.get(0);
            assertEquals(2, generalTicket.getComments().size(), "Should show all comments in general listing");
        }

        @Test
        @DisplayName("Should maintain visibility rules with multiple tickets")
        void shouldMaintainVisibilityRulesWithMultipleTickets() {
            // Given - Create multiple tickets with mixed comments
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Ticket 1", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Ticket 2", "Description 2");
            Ticket ticket1 = ticketService.createTicket(request1);
            Ticket ticket2 = ticketService.createTicket(request2);
            
            // Add mixed comments to both tickets
            ticketService.addComment(ticket1.getTicketId(), new AddCommentRequest("agent-123", "Public for user-001", "public"));
            ticketService.addComment(ticket1.getTicketId(), new AddCommentRequest("agent-123", "Internal for user-001", "internal"));
            ticketService.addComment(ticket2.getTicketId(), new AddCommentRequest("agent-456", "Public for user-002", "public"));
            ticketService.addComment(ticket2.getTicketId(), new AddCommentRequest("agent-456", "Internal for user-002", "internal"));

            // When - Customer view for user-001
            List<Ticket> user1Tickets = ticketService.listTickets(null, "user-001", null);

            // Then - Only user-001's ticket with only public comments
            assertEquals(1, user1Tickets.size());
            Ticket user1Ticket = user1Tickets.get(0);
            assertEquals("user-001", user1Ticket.getUserId());
            assertEquals(1, user1Ticket.getComments().size(), "Should show only public comments");
            assertEquals(CommentVisibility.PUBLIC, user1Ticket.getComments().get(0).visibility());
            assertEquals("Public for user-001", user1Ticket.getComments().get(0).content());
        }
    }

    @Nested
    @DisplayName("Business Rule Validation Tests")
    class BusinessRuleValidationTests {

        @Test
        @DisplayName("Should enforce 'Only public comments are visible to users' business rule")
        void shouldEnforcePublicCommentsOnlyBusinessRule() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("customer-001", "Customer issue", "Need help");
            Ticket ticket = ticketService.createTicket(request);
            
            // Add various comment types
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("customer-001", "Customer question", "public"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Agent public response", "public"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Internal agent note", "internal"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-456", "Another internal note", "internal"));

            // When - Customer requests their tickets
            List<Ticket> customerView = ticketService.listTickets(null, "customer-001", null);

            // Then - Only public comments should be visible
            assertEquals(1, customerView.size());
            Ticket customerTicket = customerView.get(0);
            assertEquals(2, customerTicket.getComments().size(), "Should show only 2 public comments");
            
            // Verify all visible comments are public
            assertTrue(customerTicket.getComments().stream()
                .allMatch(c -> c.visibility() == CommentVisibility.PUBLIC),
                "All visible comments should be public");
                
            // Verify specific comments are visible
            List<String> visibleContents = customerTicket.getComments().stream()
                .map(Comment::content)
                .toList();
            assertTrue(visibleContents.contains("Customer question"));
            assertTrue(visibleContents.contains("Agent public response"));
            assertFalse(visibleContents.contains("Internal agent note"));
            assertFalse(visibleContents.contains("Another internal note"));
        }

        @Test
        @DisplayName("Should allow agents to see all comments regardless of visibility")
        void shouldAllowAgentsToSeeAllComments() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("customer-001", "Customer issue", "Need help");
            Ticket ticket = ticketService.createTicket(request);
            ticket.setAssigneeId("agent-123");
            
            // Add various comment types
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("customer-001", "Customer question", "public"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Agent public response", "public"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Internal agent note", "internal"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-456", "Another internal note", "internal"));

            // When - Agent requests tickets assigned to them
            List<Ticket> agentView = ticketService.listTickets(null, null, "agent-123");

            // Then - All comments should be visible
            assertEquals(1, agentView.size());
            Ticket agentTicket = agentView.get(0);
            assertEquals(4, agentTicket.getComments().size(), "Should show all 4 comments");
            
            // Verify all comment types are present
            List<CommentVisibility> visibilities = agentTicket.getComments().stream()
                .map(Comment::visibility)
                .toList();
            assertEquals(2, visibilities.stream().mapToInt(v -> v == CommentVisibility.PUBLIC ? 1 : 0).sum());
            assertEquals(2, visibilities.stream().mapToInt(v -> v == CommentVisibility.INTERNAL ? 1 : 0).sum());
        }
    }
}
