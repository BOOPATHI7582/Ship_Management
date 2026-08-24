package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProformaItemRequest(
        @NotBlank @Size(max = 500) String description,
        @NotNull @DecimalMin(value = "0.0001", message = "Quantity must be positive") BigDecimal quantity,
        @Size(max = 30) String unit,
        @NotNull @DecimalMin(value = "0.0", message = "Rate cannot be negative") BigDecimal ratePerUnit
) {
}
