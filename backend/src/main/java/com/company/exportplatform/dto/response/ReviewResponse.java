package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long shipmentId,
        String shipmentRef,
        Long clientId,
        String clientName,
        int rating,
        String title,
        String reviewText,
        boolean approved,
        String moderatedByEmail,
        LocalDateTime moderatedAt,
        LocalDateTime createdAt
) {
}
