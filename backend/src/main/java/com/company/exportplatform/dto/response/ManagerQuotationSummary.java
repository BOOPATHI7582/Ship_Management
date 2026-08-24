package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ManagerQuotationSummary(
        Long id,
        String quoteNo,
        Long enquiryId,
        String enquiryRef,
        String clientCompanyName,
        String clientEmail,
        LocalDate validUntil,
        String currency,
        BigDecimal grandTotal,
        String status,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
}
