package com.company.exportplatform.dto.response;

public record ClientProfileResponse(
        Long userId,
        String email,
        String fullName,
        String companyName,
        String phone,
        String country,
        String gstin,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        java.time.LocalDateTime lastLoginAt
) {
}
