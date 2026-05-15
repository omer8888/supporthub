package com.surense.supporthub.user;

import com.surense.supporthub.user.dto.UpdateProfileRequest;
import com.surense.supporthub.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final UserService userService;

    @PutMapping("/me")
    @PreAuthorize("hasRole('AGENT')")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        User caller = userService.currentUser();
        return UserResponse.from(userService.updateProfile(caller, req));
    }
}
