package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportOverviewResponse(
        Totals totals,
        List<MonthPoint> revenueTrend,
        List<MonthPoint> invoicedTrend,
        List<NamedCount> enquiryFunnel,
        List<NamedCount> shipmentStatus,
        List<NamedCount> invoiceStatus,
        List<ClientAmount> topDebtors,
        List<ClientAmount> topClients) {

    public record Totals(
            BigDecimal totalCollected,
            BigDecimal totalInvoiced,
            BigDecimal outstanding,
            long clients,
            long activeShipments,
            long completedShipments,
            long openEnquiries,
            long pendingReviews) {
    }

    public record MonthPoint(String month, BigDecimal amount) {
    }

    public record NamedCount(String name, long count) {
    }

    public record ClientAmount(Long clientId, String clientName, BigDecimal amount) {
    }
}
