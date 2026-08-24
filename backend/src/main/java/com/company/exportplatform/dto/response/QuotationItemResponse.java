package com.company.exportplatform.dto.response;

import java.math.BigDecimal;

public record QuotationItemResponse(
        Long id,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal ratePerUnit,
        BigDecimal lineAmount
) {
}
