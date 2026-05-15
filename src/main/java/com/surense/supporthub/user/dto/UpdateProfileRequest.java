package com.surense.supporthub.user.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
        @Email String email,
        String fullName
) {}
