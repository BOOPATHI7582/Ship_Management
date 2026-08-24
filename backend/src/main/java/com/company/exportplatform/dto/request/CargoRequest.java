package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shared create/update payload. Required fields are enforced on create;
 * updates merge only the provided fields.
 */
@Getter
@Setter
public class CargoRequest {

    @Size(max = 150)
    private String name;

    private Long categoryId;

    @Size(max = 1000)
    private String description;

    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @Size(max = 10)
    private String unit;

    @Size(max = 80)
    private String originCountry;

    @Size(max = 80)
    private String destinationCountry;

    private Long loadingPortId;

    private Long destinationPortId;

    private LocalDate loadingDate;

    private LocalDate estimatedArrival;

    @Positive(message = "Price must be positive")
    private BigDecimal indicativePrice;

    @Size(max = 3)
    private String currency = "INR";

    @Pattern(regexp = "AVAILABLE|RESERVED|LOADING|IN_TRANSIT|DELIVERED",
            message = "Invalid cargo status")
    private String status = "AVAILABLE";
}
