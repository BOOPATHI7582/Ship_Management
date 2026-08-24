package com.company.exportplatform.dto.response;

public record EnquiryResponse(
        Long id,
        String referenceNo,
        String status,
        String cargoType,
        String categoryName,
        String cargoDescription,
        Long quantity,
        String unit,
        String originCountry,
        String originLocation,
        String loadingPortName,
        String loadingPortCode,
        String destinationCountry,
        String destinationLocation,
        String destinationPortName,
        String destinationPortCode,
        String requiredLoadingDate,
        String expectedDeliveryDate,
        String currency,
        java.math.BigDecimal estimatedBudget,
        java.math.BigDecimal targetPricePerUnit,
        String message,
        String createdAt
) {
}
