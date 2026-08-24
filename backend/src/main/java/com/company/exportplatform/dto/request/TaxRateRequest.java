package com.company.exportplatform.dto.request;

import com.company.exportplatform.entity.enums.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxRateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull TaxType taxType,
        @NotNull @DecimalMin(value = "0.0", message = "Rate cannot be negative") BigDecimal rate,
        @NotBlank @Size(max = 80) String country,
        @Size(max = 120) String jurisdiction,
        @NotNull LocalDate effectiveFrom,
        Boolean active,
        @Size(max = 500) String description
) {
}
