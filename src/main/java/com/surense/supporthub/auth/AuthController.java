package com.surense.supporthub.auth;

import com.surense.supporthub.auth.dto.LoginRequest;
import com.surense.supporthub.auth.dto.TokenResponse;
import com.surense.supporthub.user.User;
import com.surense.supporthub.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    /**
     *   What happens inside authenticationManager.authenticate(...)
     *
     *   That one line does this for you:
     *
     *   1. takes username "agent"
     *   2. calls CustomUserDetailsService.loadUserByUsername("agent")  → gets user from DB
     *   3. takes the password you typed
     *   4. BCrypt: does typed password match the stored hash?
     *          no  → throw BadCredentialsException → 401
     *          yes → success, continue
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        // step A: check username + password
        // [call loadUserByUsername : It loads the user. Then Spring compares the password you typed against u.getPassword() (the hash). If wrong → error → 401]
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        // step B: load the user from DB
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        return TokenResponse.bearer(jwtService.generate(user)); // step C: make token, send it back
    }
}
