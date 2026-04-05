package com.ticketmanagement.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @DocumentId
    private String id;

    private String ticketId;

    private String agentId;

    private Integer score;

    private String feedback;

    private LocalDateTime createdAt;
}
