package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProformaResponse(
        Long id,
        String piNo,
        Long quotationId,
        String quoteNo,
        Long enquiryId,
        String enquiryRef,
        Long clientId,
        String clientCompanyName,
        LocalDate issueDate,
        LocalDate validUntil,
        String currency,

        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal freightCharges,
        BigDecimal loadingCharges,
        BigDecimal documentationCharges,
        BigDecimal insuranceCharges,
        BigDecimal otherCharges,
        BigDecimal taxableAmount,
        String taxTreatment,
        String taxRateName,
        BigDecimal taxRatePercent,
        BigDecimal taxAmount,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal grandTotal,

        String paymentTerms,
        String bankDetails,
        String notes,

        String status,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        List<ProformaItemResponse> items
) {
}
