package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String actorEmail,
        String action,
        String entityType,
        Long entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        LocalDateTime createdAt) {
}
