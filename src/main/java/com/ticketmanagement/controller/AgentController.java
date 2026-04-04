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
        System.out.println("[AgentController.createAgent] Entry - Creating agent with name: '" + agent.getName() + "', email: '" + agent.getEmail() + "'");
        Agent savedAgent = agentRepository.save(agent);
        System.out.println("[AgentController.createAgent] Exit - Agent created with ID: " + savedAgent.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAgent);
    }

    @GetMapping
    public ResponseEntity<List<Agent>> getAllAgents() {
        System.out.println("[AgentController.getAllAgents] Entry - Retrieving all agents");
        List<Agent> agents = agentRepository.findAll();
        System.out.println("[AgentController.getAllAgents] Exit - Retrieved " + agents.size() + " agents");
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<Agent> getAgent(@PathVariable Long agentId) {
        System.out.println("[AgentController.getAgent] Entry - Retrieving agent " + agentId);
        return agentRepository.findById(agentId)
                .map(agent -> {
                    System.out.println("[AgentController.getAgent] Exit - Agent " + agentId + " retrieved successfully");
                    return ResponseEntity.ok(agent);
                })
                .orElseGet(() -> {
                    System.out.println("[AgentController.getAgent] Exit - Agent " + agentId + " not found");
                    return ResponseEntity.notFound().build();
                });
    }
}
