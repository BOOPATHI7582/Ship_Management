package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String link,
        boolean read,
        LocalDateTime createdAt
) {
}
