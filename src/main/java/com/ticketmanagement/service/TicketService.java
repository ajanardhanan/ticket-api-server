package com.ticketmanagement.service;

import com.ticketmanagement.dto.*;
import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.repository.AgentRepository;
import com.ticketmanagement.repository.RatingRepository;
import com.ticketmanagement.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final RatingRepository ratingRepository;

    public Ticket createTicket(CreateTicketRequest request) {
        System.out.println("[TicketService.createTicket] Entry - Creating ticket with title: '" + request.getTitle() + "', priority: " + request.getPriority());
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(Ticket.TicketStatus.OPEN);

        Ticket saved = ticketRepository.save(ticket);
        System.out.println("[TicketService.createTicket] Exit - Ticket created with ID: " + saved.getId() + ", status: " + saved.getStatus());
        return saved;
    }

    public Ticket assignTicket(String ticketId, AssignTicketRequest request) {
        System.out.println("[TicketService.assignTicket] Entry - Assigning ticket " + ticketId + " to agent " + request.getAgentId());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new RuntimeException("Agent not found with id: " + request.getAgentId()));

        ticket.setAssignedAgentId(agent.getId());
        ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);

        Ticket saved = ticketRepository.save(ticket);
        System.out.println("[TicketService.assignTicket] Exit - Ticket " + ticketId + " assigned to agent " + agent.getId() + " (" + agent.getName() + "), status: IN_PROGRESS");
        return saved;
    }

    public Ticket updateTicket(String ticketId, UpdateTicketRequest request) {
        System.out.println("[TicketService.updateTicket] Entry - Updating ticket " + ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        Ticket saved = ticketRepository.save(ticket);
        System.out.println("[TicketService.updateTicket] Exit - Ticket " + ticketId + " updated successfully");
        return saved;
    }

    public Ticket closeTicket(String ticketId) {
        System.out.println("[TicketService.closeTicket] Entry - Closing ticket " + ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setStatus(Ticket.TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        System.out.println("[TicketService.closeTicket] Exit - Ticket " + ticketId + " closed at " + saved.getClosedAt());
        return saved;
    }

    public Rating rateTicket(String ticketId, RateTicketRequest request) {
        System.out.println("[TicketService.rateTicket] Entry - Rating ticket " + ticketId + " with score: " + request.getScore());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.getAssignedAgentId() == null) {
            System.out.println("[TicketService.rateTicket] Warning - Cannot rate ticket " + ticketId + " - no assigned agent");
            throw new IllegalStateException("Cannot rate a ticket that has no assigned agent");
        }

        if (ticket.getStatus() != Ticket.TicketStatus.CLOSED) {
            System.out.println("[TicketService.rateTicket] Warning - Cannot rate ticket " + ticketId + " - status is " + ticket.getStatus() + " (must be CLOSED)");
            throw new IllegalStateException("Can only rate closed tickets");
        }

        // Check if rating already exists
        if (ratingRepository.findByTicketId(ticketId).isPresent()) {
            System.out.println("[TicketService.rateTicket] Warning - Ticket " + ticketId + " has already been rated");
            throw new IllegalStateException("Ticket has already been rated");
        }

        Rating rating = new Rating();
        rating.setTicketId(ticketId);
        rating.setAgentId(ticket.getAssignedAgentId());
        rating.setScore(request.getScore());
        rating.setFeedback(request.getFeedback());
        rating.setCreatedAt(LocalDateTime.now());

        Rating saved = ratingRepository.save(rating);
        System.out.println("[TicketService.rateTicket] Exit - Ticket " + ticketId + " rated with score " + request.getScore() + " for agent " + ticket.getAssignedAgentId());
        return saved;
    }

    public List<AgentRatingResponse> getAgentRatings() {
        System.out.println("[TicketService.getAgentRatings] Entry - Retrieving agent ratings");
        List<RatingRepository.AgentRatingProjection> projections = ratingRepository.findAgentAverageRatings();

        List<AgentRatingResponse> responses = projections.stream()
                .map(p -> new AgentRatingResponse(
                        p.getAgentId(),
                        p.getAgentName(),
                        p.getAverageScore(),
                        p.getTotalRatings()
                ))
                .collect(Collectors.toList());
        System.out.println("[TicketService.getAgentRatings] Exit - Retrieved ratings for " + responses.size() + " agents");
        return responses;
    }

    public Ticket getTicket(String ticketId) {
        System.out.println("[TicketService.getTicket] Entry - Retrieving ticket " + ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        System.out.println("[TicketService.getTicket] Exit - Ticket " + ticketId + " retrieved, status: " + ticket.getStatus());
        return ticket;
    }

    public List<Ticket> getAllTickets() {
        System.out.println("[TicketService.getAllTickets] Entry - Retrieving all tickets");
        List<Ticket> tickets = ticketRepository.findAll();
        System.out.println("[TicketService.getAllTickets] Exit - Retrieved " + tickets.size() + " tickets");
        return tickets;
    }
}
