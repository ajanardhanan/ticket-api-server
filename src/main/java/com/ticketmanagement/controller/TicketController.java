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
        Ticket ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {
        Ticket ticket = ticketService.assignTicket(ticketId, request);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {
        Ticket ticket = ticketService.updateTicket(ticketId, request);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Ticket> closeTicket(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.closeTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/rate")
    public ResponseEntity<Rating> rateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody RateTicketRequest request) {
        Rating rating = ticketService.rateTicket(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/agents/ratings")
    public ResponseEntity<List<AgentRatingResponse>> getAgentRatings() {
        List<AgentRatingResponse> ratings = ticketService.getAgentRatings();
        return ResponseEntity.ok(ratings);
    }
}
