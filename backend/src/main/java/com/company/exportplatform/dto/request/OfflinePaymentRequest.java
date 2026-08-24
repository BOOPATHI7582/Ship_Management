package com.company.exportplatform.dto.request;

import com.company.exportplatform.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OfflinePaymentRequest(
        @NotNull Long invoiceId,
        @NotNull PaymentMethod method,
        @NotBlank @Size(max = 100) String transactionReference,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String notes
) {
}
