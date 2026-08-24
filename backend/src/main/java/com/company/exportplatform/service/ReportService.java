package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.ReportOverviewResponse;
import com.company.exportplatform.dto.response.ReportOverviewResponse.ClientAmount;
import com.company.exportplatform.dto.response.ReportOverviewResponse.MonthPoint;
import com.company.exportplatform.dto.response.ReportOverviewResponse.NamedCount;
import com.company.exportplatform.entity.enums.EnquiryStatus;
import com.company.exportplatform.entity.enums.InvoiceStatus;
import com.company.exportplatform.entity.enums.PaymentStatus;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.InvoiceRepository;
import com.company.exportplatform.repository.PaymentRepository;
import com.company.exportplatform.repository.ReviewRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final List<ShipmentStatus> ACTIVE_SHIPMENT_STATUSES = List.of(
            ShipmentStatus.BOOKING_CONFIRMED, ShipmentStatus.CARGO_PREPARATION, ShipmentStatus.LOADING,
            ShipmentStatus.LOADING_COMPLETED, ShipmentStatus.DEPARTED, ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.NEAR_DESTINATION, ShipmentStatus.ARRIVED, ShipmentStatus.UNLOADING);

    private static final List<InvoiceStatus> OUTSTANDING_INVOICE_STATUSES =
            List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

    private static final List<EnquiryStatus> OPEN_ENQUIRY_STATUSES = List.of(
            EnquiryStatus.NEW, EnquiryStatus.REVIEWING, EnquiryStatus.CONTACTED,
            EnquiryStatus.NEGOTIATING, EnquiryStatus.QUOTATION_SENT, EnquiryStatus.APPROVED);

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int TREND_MONTHS = 12;

    private final ClientRepository clientRepository;
    private final EnquiryRepository enquiryRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final ShipmentRepository shipmentRepository;

    @Transactional(readOnly = true)
    public ReportOverviewResponse overview() {
        List<MonthPoint> revenueTrend = monthlySeries(paymentRepository.sumAmountByMonth(PaymentStatus.PAID));
        List<MonthPoint> invoicedTrend = monthlySeries(
                invoiceRepository.sumGrandTotalByMonth(InvoiceStatus.CANCELLED));

        BigDecimal totalCollected = totalOf(revenueTrend);
        BigDecimal totalInvoiced = totalOf(invoicedTrend);
        BigDecimal outstanding = invoiceRepository.sumBalanceExcludingStatus(InvoiceStatus.CANCELLED);

        long activeShipments = ACTIVE_SHIPMENT_STATUSES.stream()
                .mapToLong(shipmentRepository::countByStatus).sum();
        long completedShipments = shipmentRepository.countByStatus(ShipmentStatus.COMPLETED);

        Map<EnquiryStatus, Long> enquiryCounts = new HashMap<>();
        for (Object[] row : enquiryRepository.countGroupedByStatus()) {
            enquiryCounts.put((EnquiryStatus) row[0], (Long) row[1]);
        }
        long openEnquiries = OPEN_ENQUIRY_STATUSES.stream()
                .mapToLong(s -> enquiryCounts.getOrDefault(s, 0L)).sum();

        ReportOverviewResponse.Totals totals = new ReportOverviewResponse.Totals(
                totalCollected, totalInvoiced, outstanding,
                clientRepository.count(), activeShipments, completedShipments,
                openEnquiries, reviewRepository.countByApprovedFalse());

        return new ReportOverviewResponse(
                totals,
                revenueTrend,
                invoicedTrend,
                funnel(enquiryCounts),
                statusBreakdown(),
                invoiceStatusBreakdown(),
                clientAmounts(invoiceRepository.sumOutstandingByClient(OUTSTANDING_INVOICE_STATUSES, PageRequest.of(0, 5))),
                clientAmounts(paymentRepository.sumRevenueByClient(PaymentStatus.PAID, PageRequest.of(0, 5))));
    }

    private List<MonthPoint> monthlySeries(List<Object[]> rows) {
        Map<String, BigDecimal> byMonth = new HashMap<>();
        for (Object[] row : rows) {
            byMonth.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }
        YearMonth current = YearMonth.now();
        List<MonthPoint> series = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            String key = current.minusMonths(i).format(MONTH_KEY);
            series.add(new MonthPoint(key, byMonth.getOrDefault(key, BigDecimal.ZERO)));
        }
        return series;
    }

    private BigDecimal totalOf(List<MonthPoint> series) {
        return series.stream().map(MonthPoint::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<NamedCount> funnel(Map<EnquiryStatus, Long> counts) {
        List<NamedCount> list = new ArrayList<>();
        for (EnquiryStatus status : EnquiryStatus.values()) {
            list.add(new NamedCount(status.name(), counts.getOrDefault(status, 0L)));
        }
        return list;
    }

    private List<NamedCount> statusBreakdown() {
        List<NamedCount> list = new ArrayList<>();
        for (ShipmentStatus status : ShipmentStatus.values()) {
            list.add(new NamedCount(status.name(), shipmentRepository.countByStatus(status)));
        }
        return list;
    }

    private List<NamedCount> invoiceStatusBreakdown() {
        List<NamedCount> list = new ArrayList<>();
        for (InvoiceStatus status : InvoiceStatus.values()) {
            list.add(new NamedCount(status.name(), invoiceRepository.countByStatus(status)));
        }
        return list;
    }

    private List<ClientAmount> clientAmounts(List<Object[]> rows) {
        List<ClientAmount> list = new ArrayList<>();
        for (Object[] row : rows) {
            Long clientId = ((Number) row[0]).longValue();
            String name = row[1] != null ? String.valueOf(row[1]) : "Client #" + clientId;
            list.add(new ClientAmount(clientId, name, (BigDecimal) row[2]));
        }
        return list;
    }
}
