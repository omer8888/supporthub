package com.surense.supporthub.config;

import com.surense.supporthub.user.Role;
import com.surense.supporthub.user.User;
import com.surense.supporthub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
// Runs once at startup. Inserts the admin + agent accounts if they don't exist yet.
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seed("admin", Role.ADMIN, "admin@supporthub.local", "Admin User");
        seed("agent", Role.AGENT, "agent@supporthub.local", "Agent User");
    }

    private void seed(String username, Role role, String email, String fullName) {
        if (userRepository.existsByUsername(username)) return;
        User u = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password"))
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
        userRepository.save(u);
    }
}
