package com.ticketmanagement.controller;

import com.ticketmanagement.dto.*;
import com.ticketmanagement.model.Rating;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        System.out.println("[TicketController.createTicket] Entry - Creating ticket with title: '" + request.getTitle() + "', priority: " + request.getPriority());
        Ticket ticket = ticketService.createTicket(request);
        System.out.println("[TicketController.createTicket] Exit - Ticket created with ID: " + ticket.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {
        System.out.println("[TicketController.assignTicket] Entry - Assigning ticket " + ticketId + " to agent ID: " + request.getAgentId());
        Ticket ticket = ticketService.assignTicket(ticketId, request);
        System.out.println("[TicketController.assignTicket] Exit - Ticket " + ticketId + " assigned successfully");
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {
        System.out.println("[TicketController.updateTicket] Entry - Updating ticket " + ticketId);
        Ticket ticket = ticketService.updateTicket(ticketId, request);
        System.out.println("[TicketController.updateTicket] Exit - Ticket " + ticketId + " updated successfully");
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Ticket> closeTicket(@PathVariable Long ticketId) {
        System.out.println("[TicketController.closeTicket] Entry - Closing ticket " + ticketId);
        Ticket ticket = ticketService.closeTicket(ticketId);
        System.out.println("[TicketController.closeTicket] Exit - Ticket " + ticketId + " closed successfully");
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/rate")
    public ResponseEntity<Rating> rateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody RateTicketRequest request) {
        System.out.println("[TicketController.rateTicket] Entry - Rating ticket " + ticketId + " with score: " + request.getScore());
        Rating rating = ticketService.rateTicket(ticketId, request);
        System.out.println("[TicketController.rateTicket] Exit - Rating created with ID: " + rating.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long ticketId) {
        System.out.println("[TicketController.getTicket] Entry - Retrieving ticket " + ticketId);
        Ticket ticket = ticketService.getTicket(ticketId);
        System.out.println("[TicketController.getTicket] Exit - Ticket " + ticketId + " retrieved successfully");
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        System.out.println("[TicketController.getAllTickets] Entry - Retrieving all tickets");
        List<Ticket> tickets = ticketService.getAllTickets();
        System.out.println("[TicketController.getAllTickets] Exit - Retrieved " + tickets.size() + " tickets");
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/agents/ratings")
    public ResponseEntity<List<AgentRatingResponse>> getAgentRatings() {
        System.out.println("[TicketController.getAgentRatings] Entry - Retrieving agent ratings");
        List<AgentRatingResponse> ratings = ticketService.getAgentRatings();
        System.out.println("[TicketController.getAgentRatings] Exit - Retrieved ratings for " + ratings.size() + " agents");
        return ResponseEntity.ok(ratings);
    }
}
