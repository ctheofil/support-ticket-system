package com.example.ticket.service;

import com.example.ticket.dto.AddCommentRequest;
import com.example.ticket.dto.CreateTicketRequest;
import com.example.ticket.model.Comment;
import com.example.ticket.model.CommentVisibility;
import com.example.ticket.model.Ticket;
import com.example.ticket.model.TicketStatus;
import com.example.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests focused specifically on ticket listing and filtering functionality.
 * 
 * This test class covers all aspects of ticket filtering including:
 * - Filtering by status (case-insensitive)
 * - Filtering by userId (customer view)
 * - Filtering by assigneeId (agent view)
 * - Multiple filter combinations
 * - Comment visibility based on filter type
 * - Empty result handling
 */
@DisplayName("Ticket Filtering Service Tests")
class TicketFilteringServiceTest {

    private TicketService ticketService;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketRepository = new TicketRepository();
        ticketService = new TicketService(ticketRepository);
    }

    @Nested
    @DisplayName("Basic Filtering Tests")
    class BasicFilteringTests {

        @Test
        @DisplayName("Should return all tickets when no filters applied")
        void shouldReturnAllTicketsWhenNoFiltersApplied() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Subject 1", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Subject 2", "Description 2");
            ticketService.createTicket(request1);
            ticketService.createTicket(request2);

            // When
            List<Ticket> tickets = ticketService.listTickets(null, null, null);

            // Then
            assertEquals(2, tickets.size(), "Should return all tickets");
        }

        @Test
        @DisplayName("Should return empty list when no tickets exist")
        void shouldReturnEmptyListWhenNoTicketsExist() {
            // When
            List<Ticket> tickets = ticketService.listTickets(null, null, null);

            // Then
            assertTrue(tickets.isEmpty(), "Should return empty list when no tickets exist");
        }

        @Test
        @DisplayName("Should return empty list when no tickets match filters")
        void shouldReturnEmptyListWhenNoTicketsMatchFilters() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Subject", "Description");
            ticketService.createTicket(request);

            // When
            List<Ticket> tickets = ticketService.listTickets("closed", null, null);

            // Then
            assertTrue(tickets.isEmpty(), "Should return empty list when no tickets match filters");
        }
    }

    @Nested
    @DisplayName("Status Filtering Tests")
    class StatusFilteringTests {

        @Test
        @DisplayName("Should filter tickets by status (case-insensitive)")
        void shouldFilterTicketsByStatusCaseInsensitive() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Open ticket", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Another ticket", "Description 2");
            Ticket ticket1 = ticketService.createTicket(request1);
            Ticket ticket2 = ticketService.createTicket(request2);
            
            // Update one ticket to IN_PROGRESS
            ticketService.updateStatus(ticket2.getTicketId(), "in_progress");

            // When - test case insensitive filtering
            List<Ticket> openTickets = ticketService.listTickets("open", null, null);
            List<Ticket> inProgressTickets = ticketService.listTickets("IN_PROGRESS", null, null);

            // Then
            assertEquals(1, openTickets.size(), "Should return only OPEN tickets");
            assertEquals(TicketStatus.OPEN, openTickets.get(0).getStatus());
            assertEquals(1, inProgressTickets.size(), "Should return only IN_PROGRESS tickets");
            assertEquals(TicketStatus.IN_PROGRESS, inProgressTickets.get(0).getStatus());
        }

        @Test
        @DisplayName("Should filter tickets by all status types")
        void shouldFilterTicketsByAllStatusTypes() {
            // Given - Create tickets with different statuses
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Open ticket", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Progress ticket", "Description 2");
            CreateTicketRequest request3 = new CreateTicketRequest("user-003", "Resolved ticket", "Description 3");
            CreateTicketRequest request4 = new CreateTicketRequest("user-004", "Closed ticket", "Description 4");
            
            Ticket ticket1 = ticketService.createTicket(request1); // OPEN
            Ticket ticket2 = ticketService.createTicket(request2);
            Ticket ticket3 = ticketService.createTicket(request3);
            Ticket ticket4 = ticketService.createTicket(request4);
            
            ticketService.updateStatus(ticket2.getTicketId(), "in_progress");
            ticketService.updateStatus(ticket3.getTicketId(), "resolved");
            ticketService.updateStatus(ticket4.getTicketId(), "closed");

            // When
            List<Ticket> openTickets = ticketService.listTickets("open", null, null);
            List<Ticket> inProgressTickets = ticketService.listTickets("in_progress", null, null);
            List<Ticket> resolvedTickets = ticketService.listTickets("resolved", null, null);
            List<Ticket> closedTickets = ticketService.listTickets("closed", null, null);

            // Then
            assertEquals(1, openTickets.size());
            assertEquals(1, inProgressTickets.size());
            assertEquals(1, resolvedTickets.size());
            assertEquals(1, closedTickets.size());
            
            assertEquals(TicketStatus.OPEN, openTickets.get(0).getStatus());
            assertEquals(TicketStatus.IN_PROGRESS, inProgressTickets.get(0).getStatus());
            assertEquals(TicketStatus.RESOLVED, resolvedTickets.get(0).getStatus());
            assertEquals(TicketStatus.CLOSED, closedTickets.get(0).getStatus());
        }

        @Test
        @DisplayName("Should handle mixed case status filtering")
        void shouldHandleMixedCaseStatusFiltering() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "in_progress");

            // When - test various case combinations
            List<Ticket> tickets1 = ticketService.listTickets("In_Progress", null, null);
            List<Ticket> tickets2 = ticketService.listTickets("IN_progress", null, null);
            List<Ticket> tickets3 = ticketService.listTickets("in_PROGRESS", null, null);

            // Then
            assertEquals(1, tickets1.size());
            assertEquals(1, tickets2.size());
            assertEquals(1, tickets3.size());
        }
    }

    @Nested
    @DisplayName("User ID Filtering Tests")
    class UserIdFilteringTests {

        @Test
        @DisplayName("Should filter tickets by userId")
        void shouldFilterTicketsByUserId() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "User 1 ticket", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "User 2 ticket", "Description 2");
            CreateTicketRequest request3 = new CreateTicketRequest("user-001", "Another user 1 ticket", "Description 3");
            ticketService.createTicket(request1);
            ticketService.createTicket(request2);
            ticketService.createTicket(request3);

            // When
            List<Ticket> user1Tickets = ticketService.listTickets(null, "user-001", null);
            List<Ticket> user2Tickets = ticketService.listTickets(null, "user-002", null);

            // Then
            assertEquals(2, user1Tickets.size(), "Should return 2 tickets for user-001");
            assertEquals(1, user2Tickets.size(), "Should return 1 ticket for user-002");
            assertTrue(user1Tickets.stream().allMatch(t -> "user-001".equals(t.getUserId())));
            assertTrue(user2Tickets.stream().allMatch(t -> "user-002".equals(t.getUserId())));
        }

        @Test
        @DisplayName("Should return empty list for non-existent user")
        void shouldReturnEmptyListForNonExistentUser() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            ticketService.createTicket(request);

            // When
            List<Ticket> tickets = ticketService.listTickets(null, "non-existent-user", null);

            // Then
            assertTrue(tickets.isEmpty(), "Should return empty list for non-existent user");
        }

        @Test
        @DisplayName("Should filter by userId with exact match only")
        void shouldFilterByUserIdWithExactMatchOnly() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "User 1 ticket", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-0011", "Similar user ticket", "Description 2");
            CreateTicketRequest request3 = new CreateTicketRequest("user-01", "Another similar user", "Description 3");
            ticketService.createTicket(request1);
            ticketService.createTicket(request2);
            ticketService.createTicket(request3);

            // When
            List<Ticket> tickets = ticketService.listTickets(null, "user-001", null);

            // Then
            assertEquals(1, tickets.size(), "Should return only exact matches");
            assertEquals("user-001", tickets.get(0).getUserId());
        }
    }

    @Nested
    @DisplayName("Assignee ID Filtering Tests")
    class AssigneeIdFilteringTests {

        @Test
        @DisplayName("Should filter tickets by assigneeId")
        void shouldFilterTicketsByAssigneeId() {
            // Given - all tickets get default assignee "agent-123"
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Ticket 1", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Ticket 2", "Description 2");
            Ticket ticket1 = ticketService.createTicket(request1);
            ticket1.setAssigneeId("agent-123");
            Ticket ticket2 = ticketService.createTicket(request2);
            ticket2.setAssigneeId("agent-123");

            // When
            List<Ticket> agentTickets = ticketService.listTickets(null, null, "agent-123");
            List<Ticket> noAgentTickets = ticketService.listTickets(null, null, "agent-999");

            // Then
            assertEquals(2, agentTickets.size(), "Should return tickets assigned to agent-123");
            assertEquals(0, noAgentTickets.size(), "Should return no tickets for non-existent agent");
        }

        @Test
        @DisplayName("Should handle null assigneeId filtering")
        void shouldHandleNullAssigneeIdFiltering() {
            // Given - Create ticket and manually set assigneeId to null
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Unassigned ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            
            // Manually set assigneeId to null (simulating unassigned ticket)
            ticket.setAssigneeId(null);
            ticketRepository.save(ticket);

            // When
            List<Ticket> assignedTickets = ticketService.listTickets(null, null, "agent-123");
            List<Ticket> allTickets = ticketService.listTickets(null, null, null);

            // Then
            assertEquals(0, assignedTickets.size(), "Should not return tickets with null assigneeId");
            assertEquals(1, allTickets.size(), "Should return all tickets when no assignee filter");
        }
    }

    @Nested
    @DisplayName("Multiple Filter Combination Tests")
    class MultipleFilterCombinationTests {

        @Test
        @DisplayName("Should apply multiple filters simultaneously")
        void shouldApplyMultipleFiltersSimultaneously() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Ticket 1", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Ticket 2", "Description 2");
            CreateTicketRequest request3 = new CreateTicketRequest("user-001", "Ticket 3", "Description 3");
            Ticket ticket1 = ticketService.createTicket(request1);
            Ticket ticket2 = ticketService.createTicket(request2);
            Ticket ticket3 = ticketService.createTicket(request3);
            
            // Update one user-001 ticket to IN_PROGRESS
            ticketService.updateStatus(ticket3.getTicketId(), "in_progress");

            // When - filter by status=open AND userId=user-001
            List<Ticket> filteredTickets = ticketService.listTickets("open", "user-001", null);

            // Then
            assertEquals(1, filteredTickets.size(), "Should return only open tickets for user-001");
            assertEquals("user-001", filteredTickets.get(0).getUserId());
            assertEquals(TicketStatus.OPEN, filteredTickets.get(0).getStatus());
        }

        @Test
        @DisplayName("Should apply all three filters simultaneously")
        void shouldApplyAllThreeFiltersSimultaneously() {
            // Given
            CreateTicketRequest request1 = new CreateTicketRequest("user-001", "Ticket 1", "Description 1");
            CreateTicketRequest request2 = new CreateTicketRequest("user-002", "Ticket 2", "Description 2");
            CreateTicketRequest request3 = new CreateTicketRequest("user-001", "Ticket 3", "Description 3");
            Ticket ticket1 = ticketService.createTicket(request1);
            ticket1.setAssigneeId("agent-123");
            Ticket ticket2 = ticketService.createTicket(request2);
            ticket2.setAssigneeId("agent-999");
            Ticket ticket3 = ticketService.createTicket(request3);
            ticket3.setAssigneeId("agent-123");
            
            // Update one ticket status
            ticketService.updateStatus(ticket3.getTicketId(), "in_progress");

            // When - filter by status=open AND userId=user-001 AND assigneeId=agent-123
            List<Ticket> filteredTickets = ticketService.listTickets("open", "user-001", "agent-123");

            // Then
            assertEquals(1, filteredTickets.size(), "Should return tickets matching all filters");
            Ticket result = filteredTickets.get(0);
            assertEquals("user-001", result.getUserId());
            assertEquals(TicketStatus.OPEN, result.getStatus());
            assertEquals("agent-123", result.getAssigneeId());
        }

        @Test
        @DisplayName("Should return empty when multiple filters have no matches")
        void shouldReturnEmptyWhenMultipleFiltersHaveNoMatches() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            ticketService.updateStatus(ticket.getTicketId(), "closed");

            // When - filter by status=open AND userId=user-001 (ticket is closed)
            List<Ticket> filteredTickets = ticketService.listTickets("open", "user-001", null);

            // Then
            assertTrue(filteredTickets.isEmpty(), "Should return empty when no tickets match all filters");
        }
    }

    @Nested
    @DisplayName("Comment Visibility in Filtering Tests")
    class CommentVisibilityInFilteringTests {

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
        @DisplayName("Should maintain comment visibility rules with status filtering")
        void shouldMaintainCommentVisibilityRulesWithStatusFiltering() {
            // Given
            CreateTicketRequest request = new CreateTicketRequest("user-001", "Test ticket", "Description");
            Ticket ticket = ticketService.createTicket(request);
            
            // Add mixed comments
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Public comment", "public"));
            ticketService.addComment(ticket.getTicketId(), new AddCommentRequest("agent-123", "Internal comment", "internal"));

            // When - Customer view with status filter
            List<Ticket> customerTickets = ticketService.listTickets("open", "user-001", null);

            // Then - Should still apply comment visibility rules
            assertEquals(1, customerTickets.size());
            Ticket customerTicket = customerTickets.get(0);
            assertEquals(1, customerTicket.getComments().size(), "Should show only public comments even with status filter");
            assertEquals(CommentVisibility.PUBLIC, customerTicket.getComments().get(0).visibility());
        }
    }
}
