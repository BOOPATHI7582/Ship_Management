package com.company.exportplatform.dto.response;

import java.math.BigDecimal;

public record ProformaItemResponse(
        Long id,
        int itemOrder,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal ratePerUnit,
        BigDecimal lineAmount
) {
}
