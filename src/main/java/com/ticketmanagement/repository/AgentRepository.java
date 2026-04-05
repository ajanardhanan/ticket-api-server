package com.ticketmanagement.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ticketmanagement.model.Agent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class AgentRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "agents";

    public Agent save(Agent agent) {
        try {
            System.out.println("[AgentRepository.save] Entry - Saving agent: " + agent.getName());

            if (agent.getId() == null) {
                // Create new agent
                if (agent.getCreatedAt() == null) {
                    agent.setCreatedAt(LocalDateTime.now());
                }
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                agent.setId(docRef.getId());
                ApiFuture<WriteResult> result = docRef.set(agent);
                result.get();
            } else {
                // Update existing agent
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(agent.getId());
                ApiFuture<WriteResult> result = docRef.set(agent);
                result.get();
            }

            System.out.println("[AgentRepository.save] Exit - Agent saved with ID: " + agent.getId());
            return agent;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving agent", e);
        }
    }

    public Optional<Agent> findById(String id) {
        try {
            System.out.println("[AgentRepository.findById] Entry - Finding agent with ID: " + id);

            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Agent agent = document.toObject(Agent.class);
                System.out.println("[AgentRepository.findById] Exit - Agent found: " + agent.getName());
                return Optional.of(agent);
            }

            System.out.println("[AgentRepository.findById] Exit - Agent not found");
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding agent", e);
        }
    }

    public Optional<Agent> findByName(String name) {
        try {
            System.out.println("[AgentRepository.findByName] Entry - Finding agent with name: " + name);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("name", name)
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (!documents.isEmpty()) {
                Agent agent = documents.get(0).toObject(Agent.class);
                System.out.println("[AgentRepository.findByName] Exit - Agent found with ID: " + agent.getId());
                return Optional.of(agent);
            }

            System.out.println("[AgentRepository.findByName] Exit - Agent not found");
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding agent by name", e);
        }
    }

    public Optional<Agent> findByEmail(String email) {
        try {
            System.out.println("[AgentRepository.findByEmail] Entry - Finding agent with email: " + email);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (!documents.isEmpty()) {
                Agent agent = documents.get(0).toObject(Agent.class);
                System.out.println("[AgentRepository.findByEmail] Exit - Agent found with ID: " + agent.getId());
                return Optional.of(agent);
            }

            System.out.println("[AgentRepository.findByEmail] Exit - Agent not found");
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding agent by email", e);
        }
    }

    public List<Agent> findAll() {
        try {
            System.out.println("[AgentRepository.findAll] Entry - Retrieving all agents");

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = query.get();
            List<Agent> agents = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                agents.add(document.toObject(Agent.class));
            }

            System.out.println("[AgentRepository.findAll] Exit - Found " + agents.size() + " agents");
            return agents;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all agents", e);
        }
    }
}
