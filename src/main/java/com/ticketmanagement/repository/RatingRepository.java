package com.ticketmanagement.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RatingRepository {

    private final Firestore firestore;
    private final AgentRepository agentRepository;
    private static final String COLLECTION_NAME = "ratings";

    public Rating save(Rating rating) {
        try {
            System.out.println("[RatingRepository.save] Entry - Saving rating for ticket: " + rating.getTicketId());

            if (rating.getId() == null) {
                // Create new rating
                if (rating.getCreatedAt() == null) {
                    rating.setCreatedAt(LocalDateTime.now());
                }
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                rating.setId(docRef.getId());
                ApiFuture<WriteResult> result = docRef.set(rating);
                result.get();
            } else {
                // Update existing rating
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(rating.getId());
                ApiFuture<WriteResult> result = docRef.set(rating);
                result.get();
            }

            System.out.println("[RatingRepository.save] Exit - Rating saved with ID: " + rating.getId());
            return rating;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving rating", e);
        }
    }

    public Optional<Rating> findByTicketId(String ticketId) {
        try {
            System.out.println("[RatingRepository.findByTicketId] Entry - Finding rating for ticket: " + ticketId);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("ticketId", ticketId)
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (!documents.isEmpty()) {
                Rating rating = documents.get(0).toObject(Rating.class);
                System.out.println("[RatingRepository.findByTicketId] Exit - Rating found with ID: " + rating.getId());
                return Optional.of(rating);
            }

            System.out.println("[RatingRepository.findByTicketId] Exit - Rating not found");
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding rating by ticket", e);
        }
    }

    public List<Rating> findByAgentId(String agentId) {
        try {
            System.out.println("[RatingRepository.findByAgentId] Entry - Finding ratings for agent: " + agentId);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("agentId", agentId)
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<Rating> ratings = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                ratings.add(document.toObject(Rating.class));
            }

            System.out.println("[RatingRepository.findByAgentId] Exit - Found " + ratings.size() + " ratings");
            return ratings;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding ratings by agent", e);
        }
    }

    public List<AgentRatingProjection> findAgentAverageRatings() {
        try {
            System.out.println("[RatingRepository.findAgentAverageRatings] Entry - Calculating agent average ratings");

            // Get all ratings
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = query.get();
            List<Rating> allRatings = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                allRatings.add(document.toObject(Rating.class));
            }

            // Group by agent and calculate averages
            Map<String, List<Rating>> ratingsByAgent = allRatings.stream()
                    .collect(Collectors.groupingBy(Rating::getAgentId));

            List<AgentRatingProjection> projections = new ArrayList<>();

            for (Map.Entry<String, List<Rating>> entry : ratingsByAgent.entrySet()) {
                String agentId = entry.getKey();
                List<Rating> ratings = entry.getValue();

                // Get agent name
                Optional<Agent> agentOpt = agentRepository.findById(agentId);
                String agentName = agentOpt.map(Agent::getName).orElse("Unknown");

                // Calculate average
                double averageScore = ratings.stream()
                        .mapToInt(Rating::getScore)
                        .average()
                        .orElse(0.0);

                projections.add(new AgentRatingProjectionImpl(
                        agentId,
                        agentName,
                        averageScore,
                        (long) ratings.size()
                ));
            }

            // Sort by average score descending
            projections.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));

            System.out.println("[RatingRepository.findAgentAverageRatings] Exit - Calculated ratings for " + projections.size() + " agents");
            return projections;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating agent average ratings", e);
        }
    }

    public interface AgentRatingProjection {
        String getAgentId();
        String getAgentName();
        Double getAverageScore();
        Long getTotalRatings();
    }

    public static class AgentRatingProjectionImpl implements AgentRatingProjection {
        private final String agentId;
        private final String agentName;
        private final Double averageScore;
        private final Long totalRatings;

        public AgentRatingProjectionImpl(String agentId, String agentName, Double averageScore, Long totalRatings) {
            this.agentId = agentId;
            this.agentName = agentName;
            this.averageScore = averageScore;
            this.totalRatings = totalRatings;
        }

        @Override
        public String getAgentId() {
            return agentId;
        }

        @Override
        public String getAgentName() {
            return agentName;
        }

        @Override
        public Double getAverageScore() {
            return averageScore;
        }

        @Override
        public Long getTotalRatings() {
            return totalRatings;
        }
    }
}
