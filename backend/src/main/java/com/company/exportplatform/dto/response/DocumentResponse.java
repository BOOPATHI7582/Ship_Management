package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String ownerType,
        Long ownerId,
        String category,
        String title,
        String fileFormat,
        Long fileSizeBytes,
        String uploadedByEmail,
        LocalDateTime createdAt,
        String downloadUrl
) {
}
