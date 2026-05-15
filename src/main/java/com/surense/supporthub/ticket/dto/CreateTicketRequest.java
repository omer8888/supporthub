package com.surense.supporthub.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 200) String subject,
        @Size(max = 5000) String description
) {}
