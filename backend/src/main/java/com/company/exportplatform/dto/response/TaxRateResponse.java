package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaxRateResponse(
        Long id,
        String name,
        String taxType,
        BigDecimal rate,
        String country,
        String jurisdiction,
        LocalDate effectiveFrom,
        boolean active,
        String description,
        LocalDateTime createdAt
) {
}
