package com.ticketmanagement.repository;

import com.ticketmanagement.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByName(String name);
    Optional<Agent> findByEmail(String email);
}
