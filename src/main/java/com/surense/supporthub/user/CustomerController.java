package com.surense.supporthub.user;

import com.surense.supporthub.user.dto.CreateCustomerRequest;
import com.surense.supporthub.user.dto.UpdateProfileRequest;
import com.surense.supporthub.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Auth is two layers: @PreAuthorize here is the role gate;
// the service layer then does the ownership check (which rows the caller may see).
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateCustomerRequest req) {
        User caller = userService.currentUser();
        return UserResponse.from(userService.createCustomer(req, caller));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public List<UserResponse> list() {
        User caller = userService.currentUser();
        return userService.listCustomers(caller).stream().map(UserResponse::from).toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public UserResponse me() {
        return UserResponse.from(userService.currentUser());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        User caller = userService.currentUser();
        return UserResponse.from(userService.updateProfile(caller, req));
    }
}
