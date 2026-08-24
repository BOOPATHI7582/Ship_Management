package com.company.exportplatform.entity.enums;

/**
 * Configurable tax categories. Rates live in the tax_rates table and are
 * managed by admin - never hard-coded in business logic.
 */
public enum TaxType {
    CGST,
    SGST,
    IGST,
    EXEMPT,
    ZERO_RATED,
    CUSTOM
}
