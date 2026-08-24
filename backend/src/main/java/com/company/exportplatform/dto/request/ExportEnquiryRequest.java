package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Multi-step "Export Requirement" form payload (spec section 16).
 */
@Getter
@Setter
public class ExportEnquiryRequest {

    @Size(max = 150, message = "Contact name must be at most 150 characters")
    private String contactName;

    @Size(max = 150, message = "Contact email must be at most 150 characters")
    private String contactEmail;

    @Size(max = 30, message = "Contact phone must be at most 30 characters")
    private String contactPhone;

    @NotBlank(message = "Cargo type is required")
    @Size(max = 200, message = "Cargo type must be at most 200 characters")
    private String cargoType;

    private Long cargoCategoryId;

    @Size(max = 5000, message = "Cargo description must be at most 5000 characters")
    private String cargoDescription;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @Size(max = 30, message = "Unit must be at most 30 characters")
    private String unit;

    @NotBlank(message = "Origin country is required")
    @Size(max = 80)
    private String originCountry;

    @Size(max = 200, message = "Origin location must be at most 200 characters")
    private String originLocation;

    private Long loadingPortId;

    @NotBlank(message = "Destination country is required")
    @Size(max = 80)
    private String destinationCountry;

    @Size(max = 200, message = "Destination location must be at most 200 characters")
    private String destinationLocation;

    private Long destinationPortId;

    private LocalDate requiredLoadingDate;

    private LocalDate expectedDeliveryDate;

    @Size(max = 3)
    private String currency;

    @DecimalMin(value = "0", message = "Estimated budget cannot be negative")
    private BigDecimal estimatedBudget;

    @DecimalMin(value = "0", message = "Target price cannot be negative")
    private BigDecimal targetPricePerUnit;

    @Size(max = 5000, message = "Message must be at most 5000 characters")
    private String message;
}
