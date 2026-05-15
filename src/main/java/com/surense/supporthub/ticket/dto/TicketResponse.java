package com.surense.supporthub.ticket.dto;

import com.surense.supporthub.ticket.Ticket;
import com.surense.supporthub.ticket.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        TicketStatus status,
        Long userId,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getSubject(),
                t.getDescription(),
                t.getStatus(),
                t.getUser().getId(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
