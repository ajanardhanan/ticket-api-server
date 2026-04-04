package com.ticketmanagement.integration;

import com.ticketmanagement.dto.*;
import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TicketWorkflowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final int TOTAL_AGENTS = 20;
    private static final int TOTAL_TICKETS = 1000;
    private static final Random random = new Random(42); // Fixed seed for reproducibility

    @Test
    public void testCompleteTicketWorkflowWith1000Tickets() {
        System.out.println("=== Starting Integration Test: 1000 Tickets with 20 Agents ===");

        // Step 1: Create 20 agents
        System.out.println("\n[Step 1] Creating " + TOTAL_AGENTS + " agents...");
        List<Agent> agents = createAgents(TOTAL_AGENTS);
        assertEquals(TOTAL_AGENTS, agents.size(), "Should create exactly " + TOTAL_AGENTS + " agents");
        System.out.println("[Step 1] ✓ Created " + agents.size() + " agents");

        // Step 2: Create and process 1000 tickets
        System.out.println("\n[Step 2] Creating and processing " + TOTAL_TICKETS + " tickets...");
        List<Long> ticketIds = new ArrayList<>();

        for (int i = 0; i < TOTAL_TICKETS; i++) {
            // Create ticket
            Long ticketId = createTicket(i);
            ticketIds.add(ticketId);

            // Update ticket
            updateTicket(ticketId, i);

            // Assign ticket to an agent (distribute across agents)
            Agent assignedAgent = agents.get(i % TOTAL_AGENTS);
            assignTicket(ticketId, assignedAgent.getId());

            // Close ticket
            closeTicket(ticketId);

            // Rate ticket with varying scores based on agent
            int ratingScore = calculateRatingForAgent(i % TOTAL_AGENTS, i);
            rateTicket(ticketId, ratingScore);

            // Log progress every 100 tickets
            if ((i + 1) % 100 == 0) {
                System.out.println("[Step 2] Processed " + (i + 1) + "/" + TOTAL_TICKETS + " tickets");
            }
        }

        assertEquals(TOTAL_TICKETS, ticketIds.size(), "Should create exactly " + TOTAL_TICKETS + " tickets");
        System.out.println("[Step 2] ✓ Processed all " + TOTAL_TICKETS + " tickets");

        // Step 3: Get agent ratings
        System.out.println("\n[Step 3] Retrieving agent ratings...");
        ResponseEntity<List<AgentRatingResponse>> ratingsResponse = restTemplate.exchange(
                "/api/tickets/agents/ratings",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AgentRatingResponse>>() {}
        );

        assertEquals(HttpStatus.OK, ratingsResponse.getStatusCode(), "Should return 200 OK");
        assertNotNull(ratingsResponse.getBody(), "Response body should not be null");
        List<AgentRatingResponse> agentRatings = ratingsResponse.getBody();
        assertEquals(TOTAL_AGENTS, agentRatings.size(), "Should return ratings for all " + TOTAL_AGENTS + " agents");
        System.out.println("[Step 3] ✓ Retrieved ratings for " + agentRatings.size() + " agents");

        // Step 4: Verify agents are sorted in descending order by average rating
        System.out.println("\n[Step 4] Verifying agent ratings are sorted correctly...");
        System.out.println("\nAgent Ratings (sorted by average score):");
        System.out.println("Rank | Agent ID | Agent Name          | Avg Score | Total Ratings");
        System.out.println("-----|----------|---------------------|-----------|---------------");

        for (int i = 0; i < agentRatings.size(); i++) {
            AgentRatingResponse rating = agentRatings.get(i);
            System.out.printf("%4d | %8d | %-19s | %9.2f | %13d%n",
                    i + 1,
                    rating.getAgentId(),
                    rating.getAgentName(),
                    rating.getAverageScore(),
                    rating.getTotalRatings());

            // Verify descending order
            if (i > 0) {
                AgentRatingResponse previousRating = agentRatings.get(i - 1);
                assertTrue(
                        previousRating.getAverageScore() >= rating.getAverageScore(),
                        String.format("Agents should be sorted in descending order. Agent at position %d (%.2f) should have >= score than agent at position %d (%.2f)",
                                i - 1, previousRating.getAverageScore(), i, rating.getAverageScore())
                );
            }

            // Verify each agent has exactly 50 ratings (1000 tickets / 20 agents)
            assertEquals(50L, rating.getTotalRatings(),
                    "Each agent should have exactly 50 ratings (1000 tickets / 20 agents)");
        }

        System.out.println("[Step 4] ✓ All agents are correctly sorted in descending order by average rating");

        // Step 5: Verify specific constraints
        System.out.println("\n[Step 5] Verifying additional constraints...");

        // Verify all average scores are within valid range (1-5)
        for (AgentRatingResponse rating : agentRatings) {
            assertTrue(rating.getAverageScore() >= 1.0 && rating.getAverageScore() <= 5.0,
                    "Average score should be between 1 and 5");
        }
        System.out.println("[Step 5] ✓ All average scores are within valid range (1-5)");

        // Verify top agent has the highest average
        AgentRatingResponse topAgent = agentRatings.get(0);
        AgentRatingResponse bottomAgent = agentRatings.get(agentRatings.size() - 1);
        assertTrue(topAgent.getAverageScore() >= bottomAgent.getAverageScore(),
                "Top agent should have highest or equal average score");
        System.out.println("[Step 5] ✓ Top agent: " + topAgent.getAgentName() +
                " with average score: " + String.format("%.2f", topAgent.getAverageScore()));
        System.out.println("[Step 5] ✓ Bottom agent: " + bottomAgent.getAgentName() +
                " with average score: " + String.format("%.2f", bottomAgent.getAverageScore()));

        System.out.println("\n=== Integration Test Completed Successfully ===");
    }

    private List<Agent> createAgents(int count) {
        List<Agent> agents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Agent agent = new Agent();
            agent.setName("Agent " + (i + 1));
            agent.setEmail("agent" + (i + 1) + "@example.com");

            ResponseEntity<Agent> response = restTemplate.postForEntity(
                    "/api/agents",
                    agent,
                    Agent.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                    "Agent creation should return 201 CREATED");
            assertNotNull(response.getBody(), "Created agent should not be null");
            assertNotNull(response.getBody().getId(), "Created agent should have an ID");

            agents.add(response.getBody());
        }
        return agents;
    }

    private Long createTicket(int index) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Ticket #" + (index + 1));
        request.setDescription("This is test ticket number " + (index + 1));
        request.setPriority(getRandomPriority());

        ResponseEntity<Ticket> response = restTemplate.postForEntity(
                "/api/tickets",
                request,
                Ticket.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Ticket creation should return 201 CREATED");
        assertNotNull(response.getBody(), "Created ticket should not be null");
        assertNotNull(response.getBody().getId(), "Created ticket should have an ID");

        return response.getBody().getId();
    }

    private void updateTicket(Long ticketId, int index) {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setDescription("Updated description for ticket #" + (index + 1));

        restTemplate.put("/api/tickets/" + ticketId, request);
    }

    private void assignTicket(Long ticketId, Long agentId) {
        AssignTicketRequest request = new AssignTicketRequest();
        request.setAgentId(agentId);

        restTemplate.put("/api/tickets/" + ticketId + "/assign", request);
    }

    private void closeTicket(Long ticketId) {
        restTemplate.put("/api/tickets/" + ticketId + "/close", null);
    }

    private void rateTicket(Long ticketId, int score) {
        RateTicketRequest request = new RateTicketRequest();
        request.setScore(score);
        request.setFeedback("Rating: " + score + " stars");

        ResponseEntity<Rating> response = restTemplate.postForEntity(
                "/api/tickets/" + ticketId + "/rate",
                request,
                Rating.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Rating creation should return 201 CREATED");
    }

    private Ticket.TicketPriority getRandomPriority() {
        Ticket.TicketPriority[] priorities = Ticket.TicketPriority.values();
        return priorities[random.nextInt(priorities.length)];
    }

    /**
     * Calculate rating score for an agent based on a strategy that ensures different averages.
     * Agents 0-4: High ratings (mostly 4-5)
     * Agents 5-9: Medium-high ratings (mostly 3-4)
     * Agents 10-14: Medium ratings (mostly 2-3)
     * Agents 15-19: Lower ratings (mostly 1-2, with some variation)
     */
    private int calculateRatingForAgent(int agentIndex, int ticketIndex) {
        // Use ticket index to add some variation
        int variation = ticketIndex % 10;

        if (agentIndex < 5) {
            // High performers: mostly 4-5 stars
            return (variation < 8) ? 5 : 4;
        } else if (agentIndex < 10) {
            // Medium-high performers: mostly 3-4 stars
            return (variation < 6) ? 4 : 3;
        } else if (agentIndex < 15) {
            // Medium performers: mostly 2-3 stars
            return (variation < 5) ? 3 : 2;
        } else {
            // Lower performers: mostly 1-2 stars with some 3s
            if (variation < 3) {
                return 3;
            } else if (variation < 7) {
                return 2;
            } else {
                return 1;
            }
        }
    }
}
