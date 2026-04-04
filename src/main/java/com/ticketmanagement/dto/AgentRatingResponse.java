package com.ticketmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRatingResponse {

    private Long agentId;
    private String agentName;
    private Double averageScore;
    private Long totalRatings;
}
