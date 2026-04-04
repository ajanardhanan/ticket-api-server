package com.ticketmanagement.repository;

import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByTicket(Ticket ticket);
    List<Rating> findByAgent(Agent agent);

    @Query("SELECT r.agent.id as agentId, r.agent.name as agentName, AVG(r.score) as averageScore, COUNT(r) as totalRatings " +
           "FROM Rating r " +
           "GROUP BY r.agent.id, r.agent.name " +
           "ORDER BY AVG(r.score) DESC")
    List<AgentRatingProjection> findAgentAverageRatings();

    interface AgentRatingProjection {
        Long getAgentId();
        String getAgentName();
        Double getAverageScore();
        Long getTotalRatings();
    }
}
