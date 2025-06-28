package com.example.ticket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the support ticket system.
 * 
 * <p>This class provides centralized exception handling across the entire
 * application, converting business exceptions into appropriate HTTP responses
 * with proper status codes and error messages.</p>
 * 
 * <p>Handles the following exceptions:</p>
 * <ul>
 *   <li>MethodArgumentNotValidException - Bean validation failures (400 Bad Request)</li>
 *   <li>IllegalStateException - Business rule violations (500 Internal Server Error)</li>
 *   <li>IllegalArgumentException - Invalid input parameters (400 Bad Request)</li>
 *   <li>NoSuchElementException - Resource not found (404 Not Found)</li>
 * </ul>
 * 
 * <p>This handler is automatically discovered by Spring Boot's component scanning
 * and applies to all controllers in the application, providing consistent error
 * handling across all REST endpoints.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles MethodArgumentNotValidException thrown when bean validation fails.
     * 
     * <p>This occurs when @Valid annotation validation fails on request bodies,
     * such as when required fields are null or empty.</p>
     * 
     * @param ex the MethodArgumentNotValidException that was thrown
     * @return ResponseEntity with 400 Bad Request status and validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles IllegalStateException thrown when business rules are violated.
     * 
     * <p>This typically occurs when attempting invalid operations such as
     * updating a closed ticket or performing invalid status transitions.</p>
     * 
     * @param ex the IllegalStateException that was thrown
     * @return ResponseEntity with 500 Internal Server Error status and error message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse("BUSINESS_RULE_VIOLATION", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handles IllegalArgumentException thrown when invalid arguments are provided.
     * 
     * <p>This typically occurs when invalid enum values are provided for
     * status or visibility fields.</p>
     * 
     * @param ex the IllegalArgumentException that was thrown
     * @return ResponseEntity with 400 Bad Request status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles NoSuchElementException thrown when requested resources are not found.
     * 
     * <p>This typically occurs when attempting to access tickets that don't exist.</p>
     * 
     * @param ex the NoSuchElementException that was thrown
     * @return ResponseEntity with 404 Not Found status and error message
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        ErrorResponse error = new ErrorResponse("RESOURCE_NOT_FOUND", "The requested resource was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Error response DTO for consistent error formatting.
     *
     * <p>Provides a standardized structure for error responses across
     * the API, including an error code and descriptive message.</p>
     *
     * <p>This inner record ensures that all error responses follow the same
     * JSON structure, making it easier for API clients to handle errors
     * consistently.</p>
     */

    public record ErrorResponse(String code, String message) {}
}
