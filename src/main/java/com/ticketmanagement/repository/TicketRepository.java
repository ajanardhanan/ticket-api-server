package com.ticketmanagement.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ticketmanagement.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TicketRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "tickets";

    public Ticket save(Ticket ticket) {
        try {
            System.out.println("[TicketRepository.save] Entry - Saving ticket: " + ticket.getTitle());

            if (ticket.getId() == null) {
                // Create new ticket
                if (ticket.getCreatedAt() == null) {
                    ticket.setCreatedAt(LocalDateTime.now());
                }
                if (ticket.getUpdatedAt() == null) {
                    ticket.setUpdatedAt(LocalDateTime.now());
                }
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                ticket.setId(docRef.getId());
                ApiFuture<WriteResult> result = docRef.set(ticket);
                result.get();
            } else {
                // Update existing ticket
                ticket.setUpdatedAt(LocalDateTime.now());
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(ticket.getId());
                ApiFuture<WriteResult> result = docRef.set(ticket);
                result.get();
            }

            System.out.println("[TicketRepository.save] Exit - Ticket saved with ID: " + ticket.getId());
            return ticket;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving ticket", e);
        }
    }

    public Optional<Ticket> findById(String id) {
        try {
            System.out.println("[TicketRepository.findById] Entry - Finding ticket with ID: " + id);

            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Ticket ticket = document.toObject(Ticket.class);
                System.out.println("[TicketRepository.findById] Exit - Ticket found: " + ticket.getTitle());
                return Optional.of(ticket);
            }

            System.out.println("[TicketRepository.findById] Exit - Ticket not found");
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding ticket", e);
        }
    }

    public List<Ticket> findByAssignedAgentId(String agentId) {
        try {
            System.out.println("[TicketRepository.findByAssignedAgentId] Entry - Finding tickets for agent: " + agentId);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("assignedAgentId", agentId)
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                tickets.add(document.toObject(Ticket.class));
            }

            System.out.println("[TicketRepository.findByAssignedAgentId] Exit - Found " + tickets.size() + " tickets");
            return tickets;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding tickets by agent", e);
        }
    }

    public List<Ticket> findByStatus(Ticket.TicketStatus status) {
        try {
            System.out.println("[TicketRepository.findByStatus] Entry - Finding tickets with status: " + status);

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", status.name())
                    .get();
            QuerySnapshot querySnapshot = query.get();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                tickets.add(document.toObject(Ticket.class));
            }

            System.out.println("[TicketRepository.findByStatus] Exit - Found " + tickets.size() + " tickets");
            return tickets;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding tickets by status", e);
        }
    }

    public List<Ticket> findAll() {
        try {
            System.out.println("[TicketRepository.findAll] Entry - Retrieving all tickets");

            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = query.get();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                tickets.add(document.toObject(Ticket.class));
            }

            System.out.println("[TicketRepository.findAll] Exit - Found " + tickets.size() + " tickets");
            return tickets;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all tickets", e);
        }
    }
}
