package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Non-sensitive view of a cargo lot offered on the public website.
 * Never expose internal financial or management data here.
 */
public record PublicCargoResponse(
        Long id,
        String name,
        String categoryName,
        BigDecimal quantity,
        String unit,
        String originCountry,
        String destinationCountry,
        String loadingPortName,
        String loadingPortCode,
        String destinationPortName,
        String destinationPortCode,
        LocalDate loadingDate,
        LocalDate estimatedArrival,
        BigDecimal indicativePrice,
        String currency
) {
}
