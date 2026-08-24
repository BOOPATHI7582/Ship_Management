package com.company.exportplatform.entity.enums;

/**
 * Per-document tax treatment selected by authorized admin at document time.
 * Domestic intra-state -> CGST_SGST, inter-state -> IGST,
 * exports -> ZERO_RATED / EXEMPT depending on configuration.
 */
public enum TaxTreatment {
    CGST_SGST,
    IGST,
    EXEMPT,
    ZERO_RATED,
    CUSTOM
}
