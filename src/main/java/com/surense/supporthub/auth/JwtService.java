package com.surense.supporthub.auth;

import com.surense.supporthub.user.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String HMAC_ALG = "HmacSHA256";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.ttl-minutes:60}")
    private long ttlMinutes;

    public String generate(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttlMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(exp))
                .claim("roles", List.of("ROLE_" + user.getRole().name()))
                .claim("uid", user.getId())
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public SecretKey key() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG);
    }
}
