package com.company.exportplatform.dto.response;

public record DashboardSummaryResponse(
        long totalEnquiries,
        long activeShipments,
        long pendingQuotations,
        long activeNegotiations,
        long pendingPayments,
        java.math.BigDecimal outstandingAmount,
        long completedShipments
) {
}
