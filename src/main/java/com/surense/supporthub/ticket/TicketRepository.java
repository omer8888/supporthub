package com.surense.supporthub.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);

    // Tickets opened by customers that belong to this agent.
    // Match each ticket to its owning user, then keep the ones whose user is assigned to this agent.
    @Query("SELECT t FROM Ticket t, User u WHERE t.userId = u.id AND u.agentId = :agentId")
    List<Ticket> findByOwnerAgentId(Long agentId);
}
