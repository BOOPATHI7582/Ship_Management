package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CargoAdminResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        String description,
        BigDecimal quantity,
        String unit,
        String originCountry,
        String destinationCountry,
        Long loadingPortId,
        String loadingPortName,
        Long destinationPortId,
        String destinationPortName,
        LocalDate loadingDate,
        LocalDate estimatedArrival,
        BigDecimal indicativePrice,
        String currency,
        String status
) {
}
