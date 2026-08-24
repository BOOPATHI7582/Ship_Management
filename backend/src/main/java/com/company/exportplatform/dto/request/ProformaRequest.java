package com.company.exportplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A proforma invoice mirrors a quotation's commercial terms. The manager may
 * adjust charge lines and terms; all totals are recomputed server-side.
 */
public record ProformaRequest(
        Long quotationId,
        LocalDate validUntil,
        @Size(max = 1000) String paymentTerms,
        String bankDetails,
        String notes,
        @DecimalMin(value = "0.0", message = "Discount cannot be negative") BigDecimal discount,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal freightCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal loadingCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal documentationCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal insuranceCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal otherCharges,
        String taxTreatment,
        Long taxRateId,
        @Valid @NotEmpty(message = "At least one line item is required")
        List<ProformaItemRequest> items
) {
}
