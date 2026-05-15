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

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        return TokenResponse.bearer(jwtService.generate(user));
    }
}
