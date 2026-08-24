package com.company.exportplatform.security;

import com.company.exportplatform.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * HS256-signed access tokens. Claims: sub=email, uid, role, name.
 */
@Component
@Slf4j
public class JwtService {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NAME = "name";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret / JWT_SECRET is missing or shorter than 32 characters. "
                            + "Set a strong secret in backend/.env before starting the application.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_UID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().getName().name())
                .claim(CLAIM_NAME, user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Returns claims if the token signature and expiry are valid, otherwise null.
     */
    public Claims parseValidToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return null;
        }
    }
}
