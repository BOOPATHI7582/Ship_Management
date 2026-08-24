package com.company.exportplatform.dto.response;

import com.company.exportplatform.entity.enums.PaymentMethod;
import com.company.exportplatform.entity.enums.PaymentStatus;
import com.company.exportplatform.entity.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        String invoiceNo,
        Long proformaInvoiceId,
        String piNo,
        PaymentType paymentType,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt,
        String transactionReference,
        String razorpayOrderId,
        String notes,
        LocalDateTime createdAt
) {
}
