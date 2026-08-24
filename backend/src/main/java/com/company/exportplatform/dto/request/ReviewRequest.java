package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "shipmentId is required") Long shipmentId,
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5") int rating,
        @Size(max = 150) String title,
        @Size(max = 2000) String reviewText
) {
}
