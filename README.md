# Support Ticket System

## Description
A Spring Boot backend microservice for submitting and managing support tickets. This system allows users to create support tickets and enables agents to respond and update ticket statuses through a RESTful API.

## Features
- Create and manage support tickets
- Status transition management with validation
- Public and internal comment system
- Filtering and querying capabilities
- Thread-safe in-memory storage
- Comprehensive API documentation

## OpenAPI Specification
An OpenAPI Specification is located in openapi/openapi.yml

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use included Maven wrapper)

### Running the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using system Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Using Docker
```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build and run manually
docker build -t support-ticket-system .
docker run -p 8080:8080 support-ticket-system
```

## Running the Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TicketCreationServiceTest

# Run with coverage
./mvnw test jacoco:report
```
## How to Test
In the project, you can find a postman directory which contains a README file on how to test the project using a Postman collection.

## API Endpoints

### Create Ticket
```http
POST /tickets
Content-Type: application/json

{
  "userId": "user-001",
  "subject": "Payment issue",
  "description": "I was charged twice for the same order."
}
```

### List Tickets
```http
GET /tickets
GET /tickets?status=open&userId=user-001&assigneeId=agent-123
```

### Update Ticket Status
```http
PATCH /tickets/{ticketId}/status
Content-Type: application/json

{
  "status": "in_progress"
}
```

### Add Comment
```http
POST /tickets/{ticketId}/comments
Content-Type: application/json

{
  "authorId": "agent-123",
  "content": "We're currently investigating your issue.",
  "visibility": "public"
}
```

## Design Decisions

### Architecture
- **Clean Architecture**: Separation of concerns with distinct layers (Controller → Service → Repository)
- **RESTful Design**: Following REST principles for intuitive API design
- **Spring Boot**: Leveraging Spring Boot's auto-configuration and dependency injection

### Data Storage
- **In-Memory Storage**: Using `ConcurrentHashMap` for thread-safe operations
- **Thread Safety**: All repository operations are thread-safe for concurrent access
- **UUID Identifiers**: Using UUIDs for unique, distributed-system-friendly identifiers

### Business Logic
- **Status Validation**: Enforced status transitions prevent invalid state changes
- **Immutable Comments**: Comments cannot be modified once created for audit trail
- **Visibility Controls**: Public/internal comment system for different audiences

### Status Transitions
- `OPEN` → `IN_PROGRESS` (agent picks up ticket)
- `IN_PROGRESS` → `RESOLVED` (issue resolved)
- `RESOLVED` → `CLOSED` (user confirms or auto-close)
- `Any Status` → `CLOSED` (administrative closure)
- **Restriction**: Closed tickets cannot be reopened

### Comment System
- **Public Comments**: Visible to both users and agents
- **Internal Comments**: Visible only to support staff
- **Audit Trail**: All comments are timestamped and attributed

### Error Handling
- **Validation**: Input validation with meaningful error messages
- **Business Rules**: Proper exception handling for business rule violations
- **HTTP Status Codes**: Appropriate status codes for different scenarios

### Testing Strategy
- **Unit Tests**: Service layer business logic testing
- **Integration Tests**: Full API endpoint testing with MockMvc
- **Exception Handler Tests**: Comprehensive error handling validation
- **Test Coverage**: Covering ticket creation, status transitions, comment visibility, and error scenarios

### Future Considerations
- **API Design**: Enhance Update Ticket Status Api to add/update the assigneeId
- **Database Integration**: Replace in-memory storage with persistent database
- **Authentication**: Add user authentication and authorization
- **Notifications**: Email/SMS notifications for ticket updates
- **File Attachments**: Support for file uploads in tickets
- **Metrics**: Application monitoring and metrics collection

## Modern Java Features

### Records (Java 17)
The project leverages Java 17 Records for immutable data transfer objects:

```java
// Modern Record-based DTOs
public record CreateTicketRequest(String userId, String subject, String description) {}
public record UpdateStatusRequest(String status) {}
public record AddCommentRequest(String authorId, String content, String visibility) {}
public record Comment(UUID commentId, UUID ticketId, String authorId, String content, 
                     CommentVisibility visibility, LocalDateTime createdAt) {}
```

**Benefits**:
- **Immutability**: Built-in immutability for data integrity
- **Reduced Boilerplate**: No need for getters, setters, equals, hashCode, toString
- **Type Safety**: Compile-time guarantees and better IDE support
- **Performance**: JVM optimizations for record classes

### Exception Handling
Centralized exception handling with proper HTTP status code mapping:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // IllegalStateException → 500 Internal Server Error
    // IllegalArgumentException → 400 Bad Request  
    // NoSuchElementException → 404 Not Found
}
```

## Technology Stack
- **Java 17**: Modern Java features including Records for immutable data classes
- **Spring Boot 3.1.2**: Framework for rapid development
- **Maven**: Dependency management and build tool
- **JUnit 5**: Testing framework with comprehensive test coverage
- **Lombok**: Reducing boilerplate code (used selectively with Records)
- **Jackson**: JSON serialization/deserialization

## Project Structure
```
src/
├── main/java/com/example/ticket/
│   ├── TicketApplication.java          # Main application class
│   ├── controller/                     # REST controllers
│   ├── service/                        # Business logic
│   ├── repository/                     # Data access layer
│   ├── model/                          # Domain entities
│   ├── dto/                           # Data transfer objects (Records)
│   └── exception/                     # Global exception handling
└── test/java/com/example/ticket/
    ├── service/                        # Unit tests
    ├── integration/                    # Integration tests
    └── exception/                      # Exception handler tests
```
    └── integration/                    # Integration tests
```
