package com.surense.supporthub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 4, max = 100) String password,
        @Email String email,
        String fullName,
        Long agentId
) {}
