package com.surense.supporthub.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);

    @Query("select t from Ticket t where t.user.agent.id = :agentId")
    List<Ticket> findByOwnerAgentId(Long agentId);
}
