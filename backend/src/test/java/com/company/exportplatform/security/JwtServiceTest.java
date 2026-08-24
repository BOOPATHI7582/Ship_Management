package com.company.exportplatform.security;

import com.company.exportplatform.entity.Role;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("unit-test-signing-secret-that-is-long-enough-256bit!", 3_600_000L);
    }

    private User user(long id, RoleName roleName) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@test.com");
        user.setFullName("Test User");
        Role role = new Role();
        role.setName(roleName);
        user.setRole(role);
        return user;
    }

    @Test
    @DisplayName("generated token round-trips identity claims")
    void tokenRoundTrip() {
        String token = jwtService.generateToken(user(42L, RoleName.CLIENT));

        Claims claims = jwtService.parseValidToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("user42@test.com");
        assertThat(((Number) claims.get("uid")).longValue()).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("CLIENT");
        assertThat(claims.get("name", String.class)).isEqualTo("Test User");
    }

    @Test
    @DisplayName("garbage tokens are rejected as null, never thrown")
    void garbageTokenIsNull() {
        assertThat(jwtService.parseValidToken("not-a-jwt")).isNull();
        assertThat(jwtService.parseValidToken("")).isNull();
    }

    @Test
    @DisplayName("tokens signed with a different secret are rejected")
    void foreignSignatureRejected() {
        String token = jwtService.generateToken(user(1L, RoleName.ADMIN));
        JwtService other = new JwtService("a-completely-different-secret-value-256bit!!", 3_600_000L);
        assertThat(other.parseValidToken(token)).isNull();
    }

    @Test
    @DisplayName("expired tokens are rejected")
    void expiredTokenRejected() {
        JwtService expired = new JwtService(
                "unit-test-signing-secret-that-is-long-enough-256bit!", -1000L);
        String token = expired.generateToken(user(2L, RoleName.ADMIN));
        assertThat(expired.parseValidToken(token)).isNull();
    }
}
