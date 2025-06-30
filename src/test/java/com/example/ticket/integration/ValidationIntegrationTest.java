package com.example.ticket.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ticket.dto.CreateTicketRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("Should reject create ticket request with null userId")
  void testCreateTicketWithNullUserId() throws Exception {
    String requestBody = """
        {
          "userId": null,
          "subject": "Valid Subject",
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("userId: User ID is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with empty userId")
  void testCreateTicketWithEmptyUserId() throws Exception {
    String requestBody = """
        {
          "userId": "",
          "subject": "Valid Subject",
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("userId: User ID is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with blank userId")
  void testCreateTicketWithBlankUserId() throws Exception {
    String requestBody = """
        {
          "userId": "   ",
          "subject": "Valid Subject",
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("userId: User ID is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with null subject")
  void testCreateTicketWithNullSubject() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": null,
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("subject: Subject is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with empty subject")
  void testCreateTicketWithEmptySubject() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": "",
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("subject: Subject is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with blank subject")
  void testCreateTicketWithBlankSubject() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": "   ",
          "description": "Valid description"
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("subject: Subject is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with null description")
  void testCreateTicketWithNullDescription() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": "Valid Subject",
          "description": null
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("description: Description is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with empty description")
  void testCreateTicketWithEmptyDescription() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": "Valid Subject",
          "description": ""
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("description: Description is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with blank description")
  void testCreateTicketWithBlankDescription() throws Exception {
    String requestBody = """
        {
          "userId": "valid-user",
          "subject": "Valid Subject",
          "description": "   "
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value("description: Description is required and cannot be empty"));
  }

  @Test
  @DisplayName("Should reject create ticket request with multiple validation errors")
  void testCreateTicketWithMultipleValidationErrors() throws Exception {
    String requestBody = """
        {
          "userId": "",
          "subject": null,
          "description": "   "
        }
        """;

    mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("User ID is required and cannot be empty")))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Subject is required and cannot be empty")))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Description is required and cannot be empty")));
  }

  // ========== PARAMETERIZED ADD COMMENT VALIDATION TESTS ==========

  /**
   * Provides test data for add comment validation scenarios.
   * Each argument contains: field name, field value, expected error message, test description
   */
  static Stream<Arguments> addCommentValidationProvider() {
    return Stream.of(
      Arguments.of("authorId", "null", "authorId: Author ID is required and cannot be empty", "null authorId"),
      Arguments.of("authorId", "\"\"", "authorId: Author ID is required and cannot be empty", "empty authorId"),
      Arguments.of("authorId", "\"   \"", "authorId: Author ID is required and cannot be empty", "blank authorId"),
      Arguments.of("content", "null", "content: Content is required and cannot be empty", "null content"),
      Arguments.of("content", "\"\"", "content: Content is required and cannot be empty", "empty content"),
      Arguments.of("content", "\"   \"", "content: Content is required and cannot be empty", "blank content"),
      Arguments.of("visibility", "null", "visibility: Visibility is required and cannot be empty. Valid values are: public, internal", "null visibility"),
      Arguments.of("visibility", "\"\"", "visibility: Visibility is required and cannot be empty. Valid values are: public, internal", "empty visibility"),
      Arguments.of("visibility", "\"   \"", "visibility: Visibility is required and cannot be empty. Valid values are: public, internal", "blank visibility")
    );
  }

  @ParameterizedTest(name = "Should reject add comment request with {3}")
  @MethodSource("addCommentValidationProvider")
  @DisplayName("Should reject add comment requests with invalid field values")
  void testAddCommentValidation(String fieldName, String fieldValue, String expectedErrorMessage) throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Build the comment body dynamically based on which field is being tested
    String commentBody;
    if ("authorId".equals(fieldName)) {
      commentBody = String.format("""
          {
            "authorId": %s,
            "content": "Valid content",
            "visibility": "public"
          }
          """, fieldValue);
    } else if ("content".equals(fieldName)) {
      commentBody = String.format("""
          {
            "authorId": "valid-author",
            "content": %s,
            "visibility": "public"
          }
          """, fieldValue);
    } else { // visibility field
      commentBody = String.format("""
          {
            "authorId": "valid-author",
            "content": "Valid content",
            "visibility": %s
          }
          """, fieldValue);
    }

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(commentBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value(expectedErrorMessage));
  }

  @Test
  @DisplayName("Should reject add comment request with multiple validation errors")
  void testAddCommentWithMultipleValidationErrors() throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String commentBody = """
        {
          "authorId": "",
          "content": null,
          "visibility": "   "
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(commentBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Author ID is required and cannot be empty")))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Content is required and cannot be empty")))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Visibility is required and cannot be empty")));
  }

  @Test
  @DisplayName("Should reject add comment request with invalid visibility enum value")
  void testAddCommentWithInvalidVisibilityEnum() throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String commentBody = """
        {
          "authorId": "valid-author",
          "content": "Valid content",
          "visibility": "invalid"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(commentBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
      .andExpect(jsonPath("$.message").value("Invalid visibility value 'invalid'. Valid values are: public, internal"));
  }

  @Test
  @DisplayName("Should accept add comment request with valid visibility values")
  void testAddCommentWithValidVisibilityValues() throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Test with "public" visibility
    String publicCommentBody = """
        {
          "authorId": "valid-author",
          "content": "Public comment",
          "visibility": "public"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(publicCommentBody))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.comments[0].visibility").value("public"));

    // Test with "internal" visibility
    String internalCommentBody = """
        {
          "authorId": "valid-author",
          "content": "Internal comment",
          "visibility": "internal"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(internalCommentBody))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.comments[1].visibility").value("internal"));

    // Test with uppercase "PUBLIC" visibility (case-insensitive)
    String upperCaseCommentBody = """
        {
          "authorId": "valid-author",
          "content": "Uppercase comment",
          "visibility": "PUBLIC"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(upperCaseCommentBody))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.comments[2].visibility").value("public"));
  }

  // ========== UPDATE STATUS VALIDATION TESTS ==========

  @ParameterizedTest
  @MethodSource("invalidStatusTestCases")
  @DisplayName("Should reject update status request with invalid status values")
  void testUpdateStatusWithInvalidValues(String statusValue, String expectedCode, String expectedMessage) throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String statusBody = String.format("""
        {
          "status": %s
        }
        """, statusValue);

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(statusBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(expectedCode))
      .andExpect(jsonPath("$.message").value(expectedMessage));
  }

  private static Stream<Arguments> invalidStatusTestCases() {
    return Stream.of(
      Arguments.of("null", "VALIDATION_ERROR", "status: Status is required and cannot be empty. Valid values are: open, in_progress, resolved, closed"),
      Arguments.of("\"\"", "VALIDATION_ERROR", "status: Status is required and cannot be empty. Valid values are: open, in_progress, resolved, closed"),
      Arguments.of("\"invalid\"", "INVALID_ARGUMENT", "Invalid status value 'invalid'. Valid values are: open, in_progress, resolved, closed")
    );
  }

  @Test
  @DisplayName("Should accept update status request with valid status values")
  void testUpdateStatusWithValidStatusValues() throws Exception {
    // First create a ticket
    CreateTicketRequest createRequest = new CreateTicketRequest("user-001", "Test Subject", "Test description");
    String response = mockMvc.perform(post("/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    // Test with "in_progress" status
    String inProgressBody = """
        {
          "status": "in_progress"
        }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(inProgressBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("in_progress"));

    // Test with "resolved" status
    String resolvedBody = """
        {
          "status": "resolved"
        }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(resolvedBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("resolved"));

    // Test with uppercase "CLOSED" status (case-insensitive)
    String closedBody = """
        {
          "status": "CLOSED"
        }
        """;

    mockMvc.perform(patch("/tickets/" + ticketId + "/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(closedBody))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("closed"));
  }

}
