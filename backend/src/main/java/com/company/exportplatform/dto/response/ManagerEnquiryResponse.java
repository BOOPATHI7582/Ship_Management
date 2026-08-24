package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManagerEnquiryResponse(
        Long id,
        String referenceNo,
        String status,
        String clientName,
        String clientEmail,
        String companyName,
        String contactName,
        String contactPhone,
        String cargoType,
        String categoryName,
        String cargoDescription,
        BigDecimal quantity,
        String unit,
        String originCountry,
        String destinationCountry,
        String loadingPortName,
        String destinationPortName,
        String currency,
        BigDecimal estimatedBudget,
        BigDecimal targetPricePerUnit,
        String message,
        LocalDateTime createdAt
) {
}
