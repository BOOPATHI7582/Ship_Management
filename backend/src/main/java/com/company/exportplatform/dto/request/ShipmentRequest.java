package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shipment creation (booking confirmed). Client is required; the quotation,
 * enquiry, cargo and vessel links are optional operational references.
 */
public record ShipmentRequest(
        @NotNull(message = "clientId is required") Long clientId,
        Long quotationId,
        Long enquiryId,
        Long cargoId,
        Long vesselId,

        BigDecimal quantity,
        @Size(max = 30) String unit,
        @Size(max = 80) String originCountry,
        @Size(max = 80) String destinationCountry,

        Long loadingPortId,
        Long destinationPortId,
        LocalDate loadingDate,
        LocalDate estimatedArrival,

        @DecimalMin(value = "0.0", message = "Final price cannot be negative") BigDecimal finalPrice,
        String currency,
        @Size(max = 2000) String notes
) {
}
