package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        String companyName,
        String phone,
        String country,
        String role,
        boolean active,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
