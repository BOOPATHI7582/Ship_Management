package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record QuotationItemRequest(
        @NotBlank @Size(max = 500) String description,
        @NotNull @Positive BigDecimal quantity,
        @Size(max = 30) String unit,
        @NotNull @DecimalMin("0.0000") BigDecimal ratePerUnit
) {
}
