package com.ticketmanagement.service;

import com.ticketmanagement.dto.*;
import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.repository.AgentRepository;
import com.ticketmanagement.repository.RatingRepository;
import com.ticketmanagement.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(Ticket.TicketStatus.OPEN);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + request.getAgentId()));

        ticket.setAssignedAgent(agent);
        ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

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

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket closeTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        ticket.setStatus(Ticket.TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Rating rateTicket(Long ticketId, RateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getAssignedAgent() == null) {
            throw new IllegalStateException("Cannot rate a ticket that has no assigned agent");
        }

        if (ticket.getStatus() != Ticket.TicketStatus.CLOSED) {
            throw new IllegalStateException("Can only rate closed tickets");
        }

        // Check if rating already exists
        if (ratingRepository.findByTicket(ticket).isPresent()) {
            throw new IllegalStateException("Ticket has already been rated");
        }

        Rating rating = new Rating();
        rating.setTicket(ticket);
        rating.setAgent(ticket.getAssignedAgent());
        rating.setScore(request.getScore());
        rating.setFeedback(request.getFeedback());

        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public List<AgentRatingResponse> getAgentRatings() {
        List<RatingRepository.AgentRatingProjection> projections = ratingRepository.findAgentAverageRatings();

        return projections.stream()
                .map(p -> new AgentRatingResponse(
                        p.getAgentId(),
                        p.getAgentName(),
                        p.getAverageScore(),
                        p.getTotalRatings()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
}
