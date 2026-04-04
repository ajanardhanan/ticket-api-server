# Ticket Management System

A RESTful API-based ticket management system with agent rating functionality built using Spring Boot.

## Features

- **Ticket Management**: Create, assign, update, and close tickets
- **Agent Management**: Manage support agents
- **Rating System**: Rate tickets and track agent performance
- **Agent Analytics**: Get agents sorted by average ratings

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 In-Memory Database
- Lombok
- Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
cd ticket-management
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ticketdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Agent APIs

#### Create Agent
```http
POST /api/agents
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

#### Get All Agents
```http
GET /api/agents
```

#### Get Agent by ID
```http
GET /api/agents/{agentId}
```

### Ticket APIs

#### Create Ticket
```http
POST /api/tickets
Content-Type: application/json

{
  "title": "Login Issue",
  "description": "Unable to login to the system",
  "priority": "HIGH"
}
```

**Priority values**: `LOW`, `MEDIUM`, `HIGH`, `URGENT`

#### Assign Ticket
```http
PUT /api/tickets/{ticketId}/assign
Content-Type: application/json

{
  "agentId": 1
}
```

#### Update Ticket
```http
PUT /api/tickets/{ticketId}
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

**Status values**: `OPEN`, `IN_PROGRESS`, `PENDING`, `RESOLVED`, `CLOSED`

#### Close Ticket
```http
PUT /api/tickets/{ticketId}/close
```

#### Rate Ticket
```http
POST /api/tickets/{ticketId}/rate
Content-Type: application/json

{
  "score": 5,
  "feedback": "Great support!"
}
```

**Score range**: 1-5 (integer)

**Note**: Tickets can only be rated when:
- The ticket is closed
- The ticket has an assigned agent
- The ticket hasn't been rated before

#### Get Ticket by ID
```http
GET /api/tickets/{ticketId}
```

#### Get All Tickets
```http
GET /api/tickets
```

#### Get Agent Ratings (Sorted by Average Score)
```http
GET /api/tickets/agents/ratings
```

Returns agents sorted by average rating (highest first):
```json
[
  {
    "agentId": 1,
    "agentName": "John Doe",
    "averageScore": 4.5,
    "totalRatings": 10
  }
]
```

## Example Workflow

1. **Create an agent**:
```bash
curl -X POST http://localhost:8080/api/agents \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Smith","email":"jane@example.com"}'
```

2. **Create a ticket**:
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"title":"Password Reset","description":"Need to reset password","priority":"HIGH"}'
```

3. **Assign ticket to agent**:
```bash
curl -X PUT http://localhost:8080/api/tickets/1/assign \
  -H "Content-Type: application/json" \
  -d '{"agentId":1}'
```

4. **Close ticket**:
```bash
curl -X PUT http://localhost:8080/api/tickets/1/close
```

5. **Rate the ticket**:
```bash
curl -X POST http://localhost:8080/api/tickets/1/rate \
  -H "Content-Type: application/json" \
  -d '{"score":5,"feedback":"Excellent service!"}'
```

6. **Get agent ratings**:
```bash
curl http://localhost:8080/api/tickets/agents/ratings
```

## Project Structure

```
ticket-management/
├── src/
│   ├── main/
│   │   ├── java/com/ticketmanagement/
│   │   │   ├── controller/        # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── model/            # JPA entities
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   └── TicketManagementApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Building for Production

```bash
mvn clean package
java -jar target/ticket-management-1.0.0.jar
```

## Error Handling

The API includes comprehensive error handling:
- `404 NOT_FOUND`: Entity not found
- `400 BAD_REQUEST`: Invalid input or business rule violation
- `500 INTERNAL_SERVER_ERROR`: Unexpected errors

All errors return a consistent JSON format:
```json
{
  "status": 404,
  "message": "Ticket not found with id: 999",
  "timestamp": "2026-04-04T10:30:00"
}
```
