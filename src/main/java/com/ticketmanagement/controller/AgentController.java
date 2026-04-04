package com.ticketmanagement.controller;

import com.ticketmanagement.model.Agent;
import com.ticketmanagement.repository.AgentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentRepository agentRepository;

    @PostMapping
    public ResponseEntity<Agent> createAgent(@Valid @RequestBody Agent agent) {
        Agent savedAgent = agentRepository.save(agent);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAgent);
    }

    @GetMapping
    public ResponseEntity<List<Agent>> getAllAgents() {
        List<Agent> agents = agentRepository.findAll();
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<Agent> getAgent(@PathVariable Long agentId) {
        return agentRepository.findById(agentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
