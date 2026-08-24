package com.company.exportplatform.dto.response;

import com.company.exportplatform.entity.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptResponse(
        Long id,
        String receiptNo,
        Long paymentId,
        Long invoiceId,
        String invoiceNo,
        Long proformaInvoiceId,
        String piNo,
        Long clientId,
        String clientName,
        String clientCompanyName,
        LocalDate issuedOn,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        String gatewayTransactionId,
        BigDecimal remainingBalance,
        String notes,
        LocalDateTime createdAt) {
}
