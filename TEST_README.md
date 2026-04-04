# Test Documentation

## Overview

This project includes comprehensive tests covering both unit and integration testing for the Ticket Management System.

## Test Structure

```
src/test/java/com/ticketmanagement/
├── integration/
│   └── TicketWorkflowIntegrationTest.java
└── service/
    └── TicketServiceTest.java
```

## Integration Test: TicketWorkflowIntegrationTest

### Purpose
Tests the complete ticket workflow with **1000 tickets** and **20 agents**, ensuring the entire system works end-to-end.

### Test Scenario

The test creates a realistic scenario:
1. **Creates 20 agents** (Agent 1 through Agent 20)
2. **Processes 1000 tickets** through the complete lifecycle:
   - Create ticket
   - Update ticket (modify description)
   - Assign ticket to an agent (evenly distributed across 20 agents)
   - Close ticket
   - Rate ticket (with varying scores based on agent)
3. **Retrieves agent ratings** via the GET /api/tickets/agents/ratings endpoint
4. **Verifies sorting** - Agents must be returned in descending order by average rating

### Rating Strategy

To ensure meaningful test results, ratings are distributed as follows:
- **Agents 0-4**: High performers (mostly 4-5 stars)
- **Agents 5-9**: Medium-high performers (mostly 3-4 stars)
- **Agents 10-14**: Medium performers (mostly 2-3 stars)
- **Agents 15-19**: Lower performers (mostly 1-2 stars)

This creates a clear hierarchy that the test can verify.

### Assertions

The test verifies:
- ✓ Exactly 20 agents are created
- ✓ Exactly 1000 tickets are created and processed
- ✓ All 20 agents have ratings
- ✓ Each agent has exactly 50 ratings (1000 tickets / 20 agents)
- ✓ Agents are sorted in **descending order** by average rating
- ✓ All average scores are within valid range (1-5)
- ✓ Top agent has the highest average rating
- ✓ Bottom agent has the lowest average rating

### Expected Output

```
=== Starting Integration Test: 1000 Tickets with 20 Agents ===

[Step 1] Creating 20 agents...
[Step 1] ✓ Created 20 agents

[Step 2] Creating and processing 1000 tickets...
[Step 2] Processed 100/1000 tickets
[Step 2] Processed 200/1000 tickets
...
[Step 2] Processed 1000/1000 tickets
[Step 2] ✓ Processed all 1000 tickets

[Step 3] Retrieving agent ratings...
[Step 3] ✓ Retrieved ratings for 20 agents

[Step 4] Verifying agent ratings are sorted correctly...

Agent Ratings (sorted by average score):
Rank | Agent ID | Agent Name          | Avg Score | Total Ratings
-----|----------|---------------------|-----------|---------------
   1 |        1 | Agent 1             |      4.80 |            50
   2 |        2 | Agent 2             |      4.80 |            50
   3 |        3 | Agent 3             |      4.80 |            50
   ...

[Step 4] ✓ All agents are correctly sorted in descending order by average rating

[Step 5] Verifying additional constraints...
[Step 5] ✓ All average scores are within valid range (1-5)
[Step 5] ✓ Top agent: Agent 1 with average score: 4.80
[Step 5] ✓ Bottom agent: Agent 20 with average score: 1.60

=== Integration Test Completed Successfully ===
```

## Unit Test: TicketServiceTest

### Purpose
Tests individual service methods in isolation using mocks.

### Test Coverage

**Create Operations:**
- ✓ `testCreateTicket_Success` - Creates a ticket successfully

**Assign Operations:**
- ✓ `testAssignTicket_Success` - Assigns ticket to agent
- ✓ `testAssignTicket_TicketNotFound` - Handles missing ticket
- ✓ `testAssignTicket_AgentNotFound` - Handles missing agent

**Update Operations:**
- ✓ `testUpdateTicket_Success` - Updates ticket fields

**Close Operations:**
- ✓ `testCloseTicket_Success` - Closes ticket and sets timestamp

**Rating Operations:**
- ✓ `testRateTicket_Success` - Rates a closed ticket
- ✓ `testRateTicket_NoAssignedAgent` - Prevents rating without agent
- ✓ `testRateTicket_TicketNotClosed` - Prevents rating non-closed tickets
- ✓ `testRateTicket_AlreadyRated` - Prevents duplicate ratings

**Query Operations:**
- ✓ `testGetAgentRatings_Success` - Retrieves agent ratings
- ✓ `testGetTicket_Success` - Retrieves single ticket
- ✓ `testGetTicket_NotFound` - Handles missing ticket
- ✓ `testGetAllTickets_Success` - Retrieves all tickets

### Total Coverage
- **15 unit tests** covering all service methods
- **All error scenarios** tested
- **All business rules** validated

## Running the Tests

### Run All Tests
```bash
mvn test
```

### Run Only Integration Tests
```bash
mvn test -Dtest=TicketWorkflowIntegrationTest
```

### Run Only Unit Tests
```bash
mvn test -Dtest=TicketServiceTest
```

### Run Tests with Detailed Output
```bash
mvn test -X
```

### Run Tests and Generate Coverage Report
```bash
mvn clean test jacoco:report
```

## Test Execution Time

- **Unit Tests**: ~1-2 seconds (fast, uses mocks)
- **Integration Test**: ~30-60 seconds (creates 1000 tickets, full database operations)

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
- No external dependencies required
- Uses H2 in-memory database
- Deterministic results (fixed random seed)
- Clear pass/fail criteria

## Troubleshooting

### Test Fails with "Agents not sorted correctly"
**Cause**: The sorting logic in the repository query may be incorrect.
**Solution**: Check `RatingRepository.findAgentAverageRatings()` query has `ORDER BY AVG(r.score) DESC`.

### Test Fails with "Connection refused"
**Cause**: Spring Boot application didn't start properly.
**Solution**: Check application.properties and ensure H2 database is configured correctly.

### Test Timeout
**Cause**: Creating 1000 tickets may take longer on slower systems.
**Solution**: Increase test timeout or reduce ticket count for local development.

## Test Data Persistence

- **Integration Test**: Uses `@DirtiesContext` to reset database after each test
- **Unit Test**: Uses Mockito mocks, no actual database
- **H2 Database**: In-memory, automatically cleaned up after tests

## Extending the Tests

To add more test scenarios:

1. **Add more agents**: Change `TOTAL_AGENTS` constant
2. **Add more tickets**: Change `TOTAL_TICKETS` constant
3. **Modify rating distribution**: Update `calculateRatingForAgent()` method
4. **Add edge cases**: Create additional test methods

## Best Practices

✓ Tests are **independent** - can run in any order
✓ Tests are **repeatable** - same results every time
✓ Tests are **fast** - optimized for CI/CD
✓ Tests are **comprehensive** - cover happy paths and error cases
✓ Tests have **clear assertions** - easy to understand failures
