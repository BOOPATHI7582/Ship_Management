package com.company.exportplatform.dto.response;

import com.company.exportplatform.entity.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PublicTrackingResponse(
        String shipmentRef,
        String status,
        String vesselName,
        String vesselType,
        String cargoName,
        String categoryName,
        BigDecimal quantity,
        String unit,
        String originPortName,
        String originPortCode,
        String destinationPortName,
        String destinationPortCode,
        String currentLocation,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        LocalDate loadingDate,
        LocalDate estimatedArrival,
        List<TimelineEntry> timeline
) {

    public record TimelineEntry(
            ShipmentStatus status,
            String locationLabel,
            LocalDateTime occurredAt,
            String notes
    ) {
    }
}
