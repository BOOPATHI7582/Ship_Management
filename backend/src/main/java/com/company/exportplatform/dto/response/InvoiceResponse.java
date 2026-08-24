package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNo,
        String invoiceType,

        Long proformaInvoiceId,
        String piNo,
        Long quotationId,
        String quoteNo,
        Long clientId,
        String clientCompanyName,

        LocalDate issueDate,
        LocalDate dueDate,

        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingState,
        String billingPostalCode,
        String billingCountry,
        String shippingAddressLine1,
        String shippingAddressLine2,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String shippingCountry,

        String gstin,
        String pan,
        String placeOfSupply,
        String currency,
        BigDecimal exchangeRate,
        String incoterms,
        String portOfLoading,
        String portOfDischarge,
        String exportReference,

        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal freightCharges,
        BigDecimal loadingCharges,
        BigDecimal documentationCharges,
        BigDecimal insuranceCharges,
        BigDecimal otherCharges,
        BigDecimal additionalCharges,
        BigDecimal taxableAmount,
        String taxTreatment,
        String taxRateName,
        BigDecimal taxRatePercent,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal otherTaxAmount,
        BigDecimal totalTaxAmount,
        BigDecimal grandTotal,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,

        String paymentTerms,
        String bankDetails,
        String notes,
        String termsConditions,

        String status,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        List<InvoiceItemResponse> items
) {
}
