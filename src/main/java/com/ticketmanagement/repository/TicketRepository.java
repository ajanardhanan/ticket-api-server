package com.ticketmanagement.repository;

import com.ticketmanagement.model.Agent;
import com.ticketmanagement.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByAssignedAgent(Agent agent);
    List<Ticket> findByStatus(Ticket.TicketStatus status);
}
