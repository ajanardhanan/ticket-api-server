package com.ticketmanagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @DocumentId
    private String id;

    private String title;

    private String description;

    private TicketStatus status;

    private TicketPriority priority;

    private String assignedAgentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        PENDING,
        RESOLVED,
        CLOSED
    }

    public enum TicketPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
