package com.company.exportplatform.dto.response;

/**
 * Compact select-option row for staff forms (id + primary/sub labels).
 */
public record CatalogOptionResponse(
        Long id,
        String label,
        String sublabel
) {
}
