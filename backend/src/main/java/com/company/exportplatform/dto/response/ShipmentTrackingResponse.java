package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentTrackingResponse(
        Long id,
        String status,
        String locationLabel,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime occurredAt,
        String notes
) {
}
