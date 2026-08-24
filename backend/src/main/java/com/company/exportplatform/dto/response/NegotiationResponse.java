package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NegotiationResponse(
        Long threadId,
        String status,
        BigDecimal agreedPrice,
        LocalDateTime closedAt,
        List<NegotiationMessageResponse> messages
) {
    public record NegotiationMessageResponse(
            Long id,
            String senderType,
            String senderName,
            BigDecimal offerPrice,
            String message,
            String status,
            LocalDateTime createdAt
    ) {
    }
}
