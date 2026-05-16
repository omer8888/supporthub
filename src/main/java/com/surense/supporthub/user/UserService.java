package com.surense.supporthub.user;

import com.surense.supporthub.exception.ConflictException;
import com.surense.supporthub.exception.ForbiddenException;
import com.surense.supporthub.exception.NotFoundException;
import com.surense.supporthub.user.dto.CreateCustomerRequest;
import com.surense.supporthub.user.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Reads the logged-in username from the JWT (via SecurityContext) and loads the User entity.
    public User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createCustomer(CreateCustomerRequest req, User caller) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already exists");
        }
        User agent = resolveAgent(caller, req.agentId());
        User customer = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .email(req.email())
                .fullName(req.fullName())
                .role(Role.CUSTOMER)
                .agentId(agent.getId())
                .build();
        return userRepository.save(customer);
    }

    // Decides which agent owns the new customer:
    // AGENT caller -> themselves. ADMIN caller -> the agentId they passed in the request.
    private User resolveAgent(User caller, Long agentId) {
        if (caller.getRole() == Role.AGENT) return caller;
        if (caller.getRole() == Role.ADMIN) {
            if (agentId == null) throw new ConflictException("agentId required for admin");
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new NotFoundException("Agent not found"));
            if (agent.getRole() != Role.AGENT) throw new ConflictException("Target user is not an agent");
            return agent;
        }
        throw new ForbiddenException("Cannot create customer");
    }

    // Ownership filter: an agent sees only their own customers; an admin sees all.
    public List<User> listCustomers(User caller) {
        return switch (caller.getRole()) {
            case AGENT -> userRepository.findByAgentId(caller.getId()); // agent: only his
            case ADMIN -> userRepository.findByRole(Role.CUSTOMER); // admin: all
            default -> throw new ForbiddenException("Cannot list customers");
        };
    }

    //email and full name update
    public User updateProfile(User caller, UpdateProfileRequest req) {
        if (req.email() != null) caller.setEmail(req.email());
        if (req.fullName() != null) caller.setFullName(req.fullName());
        return userRepository.save(caller);
    }
}
