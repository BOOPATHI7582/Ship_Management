package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShipmentResponse(
        Long id,
        String shipmentRef,
        String status,

        Long clientId,
        String clientCompanyName,
        String clientEmail,

        Long quotationId,
        String quoteNo,
        Long enquiryId,
        Long cargoId,
        String cargoName,
        Long vesselId,
        String vesselName,
        String vesselImoNumber,

        BigDecimal quantity,
        String unit,
        String originCountry,
        String destinationCountry,

        Long loadingPortId,
        String loadingPortName,
        String loadingPortCode,
        Long destinationPortId,
        String destinationPortName,
        String destinationPortCode,

        LocalDate loadingDate,
        LocalDate estimatedArrival,
        LocalDate actualArrival,
        LocalDateTime deliveredAt,

        BigDecimal finalPrice,
        String currency,

        String currentLocation,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        LocalDateTime lastTrackedAt,

        String notes,
        LocalDateTime createdAt,
        List<ShipmentTrackingResponse> timeline
) {
}
