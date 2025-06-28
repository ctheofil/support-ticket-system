package com.example.ticket.integration;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
class TicketIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("Test Create Ticket")
  @Test
  void testCreateTicket() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest("user-101", "Integration Test", "Integration test description");

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.subject").value("Integration Test"));
  }

  @DisplayName("Test List Tickets With Filters")
  @Test
  void testListTicketsWithFilters() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest("filter-user", "Filter Test", "Filter test description");

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());

    mockMvc.perform(get("/tickets")
        .param("userId", "filter-user"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].userId").value("filter-user"));
  }

  @DisplayName("Test Update Ticket Status")
  @Test
  void testUpdateTicketStatus() throws Exception {
    CreateTicketRequest createRequest = new CreateTicketRequest("status-user", "Status Test", "Status test description");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String patchBody = """
        {
          "status": "in_progress"
        }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(patchBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("in_progress"));
  }

  @DisplayName("Test Invalid Status- Transition from Closed")
  @Test
  void testInvalidStatusTransitionFromClosed() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest("closed-user", "Closed Test", "Closing this");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Close ticket
    String close = """
        { "status": "closed" }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(close))
      .andExpect(status().isOk());

    // Try to reopen it
    String reopen = """
        { "status": "in_progress" }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(reopen))
      .andExpect(status().isInternalServerError());
  }

  @DisplayName("Test Add Internal Comment")
  @Test
  void testAddInternalComment() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest("comment-user", "Internal Comment", "Check internal visibility");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String commentBody = """
        {
          "authorId": "agent-007",
          "content": "Internal note",
          "visibility": "internal"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(commentBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.comments[0].visibility").value("internal"));
  }

  @Test
  @DisplayName("Should implement Only public comments are visible to users")
  void testCommentVisibilityBusinessRule() throws Exception {
    // Step 1: Create a ticket
    CreateTicketRequest request = new CreateTicketRequest("user-001", "Visibility Test", "Testing comment visibility");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Step 2: Add a public comment
    String publicComment = """
            {
              "authorId": "agent-123",
              "content": "This is a public comment visible to users",
              "visibility": "public"
            }
            """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(publicComment))
      .andExpect(status().isOk());

    // Step 3: Add an internal comment
    String internalComment = """
            {
              "authorId": "agent-123",
              "content": "This is an internal comment only for agents",
              "visibility": "internal"
            }
            """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(internalComment))
      .andExpect(status().isOk());

    // Step 4: Request tickets with userId filter (customer request) - should see only public comment
    mockMvc.perform(get("/tickets")
        .param("userId", "user-001"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments").isArray())
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments.length()").value(1))
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments[0].content").value("This is a public comment visible to users"))
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments[0].visibility").value("public"));

    // Step 5: Request without filters (general listing) - should see all comments
    // Use JSONPath to find our specific ticket by ticketId and verify it has 2 comments
    mockMvc.perform(get("/tickets"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments.length()").value(2));
  }

  @Test
  @DisplayName("Should work with other filtering parameters")
  void testCommentVisibilityWithOtherFilters() throws Exception {
    // Create a ticket
    CreateTicketRequest request = new CreateTicketRequest("user-002", "Filter Test", "Testing with filters");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Add mixed comments
    String publicComment = """
            {
              "authorId": "agent-456",
              "content": "Public response for user-002",
              "visibility": "public"
            }
            """;

    String internalComment = """
            {
              "authorId": "agent-456",
              "content": "Internal note for user-002",
              "visibility": "internal"
            }
            """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(publicComment))
      .andExpect(status().isOk());

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(internalComment))
      .andExpect(status().isOk());

    // Filter by userId and status (customer request) - should see only public comments
    mockMvc.perform(get("/tickets")
        .param("userId", "user-002")
        .param("status", "open"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].userId").value("user-002"))
      .andExpect(jsonPath("$[0].comments.length()").value(1))
      .andExpect(jsonPath("$[0].comments[0].visibility").value("public"));
  }

  @Test
  @DisplayName("Should show all comments in general listing (no specific filters)")
  void testGeneralListingShowsAllComments() throws Exception {
    // Create ticket with mixed comments
    CreateTicketRequest request = new CreateTicketRequest("user-003", "General Listing", "Testing general listing");

    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Add both types of comments
    String publicComment = """
            {
              "authorId": "agent-789",
              "content": "Public comment",
              "visibility": "public"
            }
            """;

    String internalComment = """
            {
              "authorId": "agent-789",
              "content": "Internal comment",
              "visibility": "internal"
            }
            """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(publicComment))
      .andExpect(status().isOk());

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(internalComment))
      .andExpect(status().isOk());

    // Request without filters - should see all comments (general listing)
    // Use JSONPath to find our specific ticket by ticketId and verify it has 2 comments
    mockMvc.perform(get("/tickets"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.ticketId=='" + ticketId + "')].comments.length()").value(2)); // All comments visible
  }

}
