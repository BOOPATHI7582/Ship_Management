package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Assignment edit: attach/change vessel, cargo, ports, dates and price.
 * Null fields leave existing values untouched.
 */
public record ShipmentUpdateRequest(
        Long cargoId,
        Long vesselId,
        Long loadingPortId,
        Long destinationPortId,
        LocalDate loadingDate,
        LocalDate estimatedArrival,
        @DecimalMin(value = "0.0", message = "Final price cannot be negative") BigDecimal finalPrice,
        @Size(max = 2000) String notes
) {
}
