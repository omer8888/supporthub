package com.surense.supporthub.auth;

import com.surense.supporthub.user.User;
import com.surense.supporthub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    //checks if user exists,
    @Override
    public UserDetails loadUserByUsername(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(), // the BCrypt hash
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}
