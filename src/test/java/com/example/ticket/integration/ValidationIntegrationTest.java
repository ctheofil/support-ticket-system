package com.example.ticket.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
      Arguments.of("content", "\"   \"", "content: Content is required and cannot be empty", "blank content")
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
      .andExpect(status().isOk())
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
    } else { // content field
      commentBody = String.format("""
          {
            "authorId": "valid-author",
            "content": %s,
            "visibility": "public"
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
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("ticketId").asText());

    String commentBody = """
        {
          "authorId": "",
          "content": null,
          "visibility": "public"
        }
        """;

    mockMvc.perform(post("/tickets/" + ticketId + "/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(commentBody))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Author ID is required and cannot be empty")))
      .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Content is required and cannot be empty")));
  }

}
