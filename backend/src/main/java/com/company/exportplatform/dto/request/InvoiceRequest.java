package com.company.exportplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tax invoice creation. Issued from a proforma invoice (preferred) or directly
 * from an accepted quotation. Invoices are legal documents: no draft state -
 * creation issues the document immediately; corrections happen by cancelling
 * and re-issuing.
 */
public record InvoiceRequest(
        Long proformaInvoiceId,
        Long quotationId,
        String invoiceType,
        LocalDate dueDate,
        @Size(max = 120) String placeOfSupply,
        BigDecimal exchangeRate,
        @Size(max = 150) String portOfLoading,
        @Size(max = 150) String portOfDischarge,
        @Size(max = 100) String exportReference,

        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal discount,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal freightCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal loadingCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal documentationCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal insuranceCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal otherCharges,
        @DecimalMin(value = "0.0", message = "Charges cannot be negative") BigDecimal additionalCharges,

        String taxTreatment,

        @Size(max = 1000) String paymentTerms,
        String bankDetails,
        String notes,
        String termsConditions,

        @Valid @NotEmpty(message = "At least one line item is required")
        List<InvoiceItemRequest> items
) {
}
