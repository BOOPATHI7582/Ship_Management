package com.company.exportplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QuotationRequest(
        @NotNull Long enquiryId,
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter ISO code") String currency,
        LocalDate validUntil,
        @Size(max = 20) String incoterms,
        @Size(max = 1000) String paymentTerms,
        @Size(max = 1000) String deliveryTerms,
        @Size(max = 4000) String notes,
        @Size(max = 4000) String termsConditions,

        // Optional billing snapshot overrides; defaults come from the client profile
        @Size(max = 255) String billingAddressLine1,
        @Size(max = 255) String billingAddressLine2,
        @Size(max = 100) String billingCity,
        @Size(max = 100) String billingState,
        @Size(max = 20) String billingPostalCode,
        @Size(max = 80) String billingCountry,

        @DecimalMin("0.0000") BigDecimal discount,
        @DecimalMin("0.0000") BigDecimal freightCharges,
        @DecimalMin("0.0000") BigDecimal loadingCharges,
        @DecimalMin("0.0000") BigDecimal documentationCharges,
        @DecimalMin("0.0000") BigDecimal insuranceCharges,
        @DecimalMin("0.0000") BigDecimal otherCharges,

        /** Explicit treatment (CGST_SGST, IGST, EXEMPT, ZERO_RATED, CUSTOM). Null means untaxed/exempt. */
        @Size(max = 40) String taxTreatment,
        Long taxRateId,

        @Valid @NotEmpty List<QuotationItemRequest> items
) {
}
