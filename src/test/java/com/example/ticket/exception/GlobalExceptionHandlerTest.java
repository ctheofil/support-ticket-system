package com.example.ticket.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle IllegalStateException with 500 status")
    void testHandleIllegalStateException() {
        // Given
        String errorMessage = "Cannot update closed ticket";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleIllegalStateException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().code());
        assertEquals(errorMessage, response.getBody().message());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void testHandleIllegalArgumentException() {
        // Given
        String errorMessage = "No enum constant com.example.ticket.model.TicketStatus.INVALID";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().code());
        assertEquals(errorMessage, response.getBody().message());
    }

    @Test
    @DisplayName("Should handle NoSuchElementException with 404 status")
    void testHandleNoSuchElementException() {
        // Given
        String originalMessage = "No value present";
        NoSuchElementException exception = new NoSuchElementException(originalMessage);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleNoSuchElementException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
        assertEquals("The requested resource was not found", response.getBody().message());
    }

    @Test
    @DisplayName("Should create ErrorResponse with correct structure")
    void testErrorResponseStructure() {
        // Given
        String code = "TEST_ERROR";
        String message = "Test error message";

        // When
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(code, message);

        // Then
        assertEquals(code, errorResponse.code());
        assertEquals(message, errorResponse.message());
    }

    @Test
    @DisplayName("Should handle exceptions with null messages")
    void testHandleExceptionWithNullMessage() {
        // Given
        IllegalStateException exception = new IllegalStateException((String) null);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleIllegalStateException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().code());
        assertNull(response.getBody().message());
    }

    @Test
    @DisplayName("Should handle exceptions with empty messages")
    void testHandleExceptionWithEmptyMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().code());
        assertEquals("", response.getBody().message());
    }

    @Test
    @DisplayName("Should assign different error codes for different exception types")
    void testDifferentErrorCodesForDifferentExceptions() {
        // Given
        IllegalStateException stateException = new IllegalStateException("State error");
        IllegalArgumentException argumentException = new IllegalArgumentException("Argument error");
        NoSuchElementException elementException = new NoSuchElementException("Element error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> stateResponse = exceptionHandler.handleIllegalStateException(stateException);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> argumentResponse = exceptionHandler.handleIllegalArgumentException(argumentException);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> elementResponse = exceptionHandler.handleNoSuchElementException(elementException);

        // Then
      assertNotNull(stateResponse.getBody());
      assertNotNull(argumentResponse.getBody());
      assertNotNull(elementResponse.getBody());
      assertNotEquals(stateResponse.getBody().code(), argumentResponse.getBody().code());
      assertNotEquals(stateResponse.getBody().code(), elementResponse.getBody().code());
      assertNotEquals(argumentResponse.getBody().code(), elementResponse.getBody().code());
        
        assertEquals("BUSINESS_RULE_VIOLATION", stateResponse.getBody().code());
        assertEquals("INVALID_ARGUMENT", argumentResponse.getBody().code());
        assertEquals("RESOURCE_NOT_FOUND", elementResponse.getBody().code());
    }
}
