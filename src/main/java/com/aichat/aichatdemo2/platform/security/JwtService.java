package com.aichat.aichatdemo2.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] secret = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes");
        }
        key = Keys.hmacShaKeyFor(secret);
    }

    public String issue(UUID userId, String login) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("login", login)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getTtl())))
                .signWith(key)
                .compact();
    }

    public AppPrincipal parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String login = claims.get("login", String.class);
        return new AppPrincipal(UUID.fromString(claims.getSubject()), login);
    }
}
