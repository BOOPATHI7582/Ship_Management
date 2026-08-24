package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record AdminClientResponse(
        Long userId,
        String email,
        String fullName,
        String companyName,
        String phone,
        String country,
        boolean active,
        String gstin,
        String city,
        String state,
        LocalDateTime lastLoginAt,
        LocalDateTime registeredAt
) {
}
