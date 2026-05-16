package com.surense.supporthub.user.dto;

import com.surense.supporthub.user.Role;
import com.surense.supporthub.user.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Role role,
        Long agentId
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.getAgentId()
        );
    }
}
