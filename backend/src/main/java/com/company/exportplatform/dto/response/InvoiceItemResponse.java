package com.company.exportplatform.dto.response;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long id,
        int itemOrder,
        String description,
        String hsnCode,
        BigDecimal quantity,
        String unit,
        BigDecimal ratePerUnit,
        BigDecimal lineAmount
) {
}
