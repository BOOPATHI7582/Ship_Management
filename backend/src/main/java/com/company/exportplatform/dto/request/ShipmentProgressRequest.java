package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Progress update: appends a tracking entry and (optionally) advances the
 * lifecycle state forward. A null status logs a location ping without a state
 * change.
 */
public record ShipmentProgressRequest(
        String status,
        @Size(max = 200) String locationLabel,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime occurredAt,
        @Size(max = 1000) String notes
) {
}
