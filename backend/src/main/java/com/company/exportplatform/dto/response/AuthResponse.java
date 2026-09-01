package com.company.exportplatform.dto.response;

import com.company.exportplatform.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInMs;
    private UserResponse user;
    private boolean requiresOtp;
    private String devOtp;

    public static AuthResponse of(String token, long expirationMs, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(expirationMs)
                .user(UserResponse.from(user))
                .build();
    }

    public static AuthResponse otpPending(User user, String devOtp) {
        return AuthResponse.builder()
                .requiresOtp(true)
                .devOtp(devOtp)
                .user(UserResponse.from(user))
                .build();
    }
}
