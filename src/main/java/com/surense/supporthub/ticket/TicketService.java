package com.surense.supporthub.ticket;

import com.surense.supporthub.exception.ForbiddenException;
import com.surense.supporthub.ticket.dto.CreateTicketRequest;
import com.surense.supporthub.user.Role;
import com.surense.supporthub.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    // The ticket owner is always the caller — a customer can't open a ticket for someone else.
    public Ticket create(CreateTicketRequest req, User caller) {
        if (caller.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("Only customers can create tickets");
        }
        Ticket t = Ticket.builder()
                .subject(req.subject())
                .description(req.description())
                .status(TicketStatus.OPEN)
                .userId(caller.getId())
                .build();
        return ticketRepository.save(t);
    }

    // Ownership filter: customer sees own tickets; agent sees their customers' tickets; admin sees all.
    public List<Ticket> list(User caller) {
        return switch (caller.getRole()) {
            case CUSTOMER -> ticketRepository.findByUserId(caller.getId());
            case AGENT -> ticketRepository.findByOwnerAgentId(caller.getId());
            case ADMIN -> ticketRepository.findAll();
        };
    }
}
