# Support Ticket System - Postman Collection

This directory contains a comprehensive Postman collection for testing the Support Ticket System API.

## Files

- `Support-Ticket-System.postman_collection.json` - Main collection with all API requests
- `Support-Ticket-System.postman_environment.json` - Environment variables for local testing
- `README.md` - This documentation file

## Collection Overview

The collection is organized into 5 main categories:

### 1. Create Tickets
- **Create Basic Ticket** - Creates a payment issue ticket (matches README example)
- **Create Technical Issue Ticket** - Creates a login problem ticket
- **Create Account Issue Ticket** - Creates an account suspension inquiry

### 2. List Tickets
- **List All Tickets** - Retrieves all tickets without filters
- **List Open Tickets** - Filters by status=open
- **List Tickets by User** - Filters by userId (customer view - only public comments)
- **List Tickets by Assignee** - Filters by assigneeId (agent view - all comments)
- **List with Multiple Filters** - Combines status and userId filters

### 3. Update Ticket Status
- **Update to In Progress** - Changes status from OPEN to IN_PROGRESS
- **Update to Resolved** - Changes status to RESOLVED
- **Update to Closed** - Changes status to CLOSED

### 4. Add Comments
- **Add Public Comment** - Adds a public comment (matches README example)
- **Add Internal Comment** - Adds an internal comment (agent-only visibility)
- **Add Customer Response** - Simulates customer adding a response

## How to Use

### Prerequisites
1. Ensure your Support Ticket System is running on `http://localhost:8080`
2. Have Postman installed

### Import Instructions
1. Open Postman
2. Click "Import" button
3. Select both JSON files:
   - `Support-Ticket-System.postman_collection.json`
   - `Support-Ticket-System.postman_environment.json`
4. Select the "Support Ticket System - Local" environment

### Running the Tests

#### Option 1: Run Individual Requests
- Navigate through the collection folders
- Click on any request to view details
- Click "Send" to execute the request
- Check the "Test Results" tab for automated validations

#### Option 2: Run the Entire Collection
1. Right-click on the collection name
2. Select "Run collection"
3. Choose which requests to run
4. Click "Run Support Ticket System API"

#### Option 3: Run in Sequence (Recommended)
For best results, run requests in this order:
1. **Create Tickets** folder (all 3 requests) - This populates ticket IDs
2. **List Tickets** folder - Test filtering and querying
3. **Add Comments** folder - Add comments to created tickets
4. **Update Ticket Status** folder - Test status transitions

### Environment Variables

The collection uses these environment variables:
- `baseUrl` - API base URL (default: http://localhost:8080)
- `basicTicketId` - ID of the basic payment issue ticket (auto-populated)
- `techTicketId` - ID of the technical issue ticket (auto-populated)
- `accountTicketId` - ID of the account issue ticket (auto-populated)

Ticket IDs are automatically captured from creation responses and used in subsequent requests.

### Test Validations

Each request includes automated tests that verify:
- **HTTP Status Codes** - Correct response codes (200, 400, 404, 500)
- **Response Structure** - Required fields are present
- **Business Logic** - Status transitions, comment visibility, etc.
- **Data Integrity** - Timestamps, IDs, and relationships

### Key Features Tested

#### Status Transitions
- OPEN → IN_PROGRESS → RESOLVED → CLOSED
- Validation that closed tickets cannot be reopened

#### Comment Visibility
- **Customer View** (userId filter): Only public comments visible
- **Agent View** (assigneeId filter): All comments visible
- **Public Comments**: Visible to both users and agents
- **Internal Comments**: Visible only to support staff

#### Filtering and Querying
- Filter by status (open, in_progress, resolved, closed)
- Filter by userId (customer tickets)
- Filter by assigneeId (agent assignments)
- Multiple filter combinations

### Sample Request Bodies

#### Create Ticket
```json
{
  "userId": "user-001",
  "subject": "Payment issue",
  "description": "I was charged twice for the same order."
}
```

#### Update Status
```json
{
  "status": "in_progress"
}
```

#### Add Comment
```json
{
  "authorId": "agent-123",
  "content": "We're currently investigating your issue.",
  "visibility": "public"
}
```

### Expected Response Structure

#### Ticket Response
```json
{
  "ticketId": "uuid",
  "subject": "string",
  "description": "string",
  "status": "OPEN|IN_PROGRESS|RESOLVED|CLOSED",
  "userId": "string",
  "assigneeId": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "comments": [...]
}
```

#### Comment Structure
```json
{
  "commentId": "uuid",
  "ticketId": "uuid",
  "authorId": "string",
  "content": "string",
  "visibility": "PUBLIC|INTERNAL",
  "createdAt": "timestamp"
}
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure the Spring Boot application is running
   - Check that it's accessible at http://localhost:8080

2. **404 Errors on Status Updates**
   - Run the "Create Tickets" requests first to populate ticket IDs
   - Check that environment variables are set correctly

3. **Test Failures**
   - Verify the application is running the latest code
   - Check that responses match expected structure
   - Ensure proper sequence of requests (create before update)

### Debugging Tips

1. **Check Environment Variables**
   - View current environment in top-right dropdown
   - Verify ticket IDs are populated after creation requests

2. **Review Response Bodies**
   - Check the response tab for actual API responses
   - Compare with expected structure in tests

3. **Console Logs**
   - Open Postman Console (View → Show Postman Console)
   - Review detailed request/response information

## Integration with Development Workflow

This collection can be used for:
- **Manual Testing** - Verify API functionality during development
- **Regression Testing** - Ensure changes don't break existing functionality
- **API Documentation** - Demonstrate API usage with real examples
- **CI/CD Integration** - Run automated tests in build pipelines

## Extending the Collection

To add new test scenarios:
1. Create new requests in appropriate folders
2. Add test scripts for validation
3. Update environment variables if needed
4. Document new scenarios in this README