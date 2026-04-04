package com.ticketmanagement.dto;

import com.ticketmanagement.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequest {

    private String title;
    private String description;
    private Ticket.TicketStatus status;
    private Ticket.TicketPriority priority;
}
