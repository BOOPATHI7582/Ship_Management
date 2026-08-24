package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record QuotationResponse(
        Long id,
        String quoteNo,
        Long enquiryId,
        String enquiryRef,
        Long clientId,
        String clientCompanyName,
        LocalDate quotationDate,
        LocalDate validUntil,
        String currency,
        String incoterms,
        String paymentTerms,
        String deliveryTerms,
        String notes,
        String termsConditions,

        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingState,
        String billingPostalCode,
        String billingCountry,

        String contactEmail,
        String contactPhone,

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

        String status,
        String secureToken,
        LocalDateTime sentAt,
        LocalDateTime viewedAt,
        LocalDateTime acceptedAt,
        LocalDateTime rejectedAt,
        String rejectionReason,
        LocalDateTime createdAt,
        List<QuotationItemResponse> items
) {
}
