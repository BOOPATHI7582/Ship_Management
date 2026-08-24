package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.DashboardSummaryResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.InvoiceStatus;
import com.company.exportplatform.entity.enums.NegotiationThreadStatus;
import com.company.exportplatform.entity.enums.PaymentStatus;
import com.company.exportplatform.entity.enums.QuotationStatus;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.InvoiceRepository;
import com.company.exportplatform.repository.NegotiationRepository;
import com.company.exportplatform.repository.PaymentRepository;
import com.company.exportplatform.repository.QuotationRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<ShipmentStatus> ACTIVE_SHIPMENT_STATUSES = List.of(
            ShipmentStatus.BOOKING_CONFIRMED, ShipmentStatus.CARGO_PREPARATION, ShipmentStatus.LOADING,
            ShipmentStatus.LOADING_COMPLETED, ShipmentStatus.DEPARTED, ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.NEAR_DESTINATION, ShipmentStatus.ARRIVED, ShipmentStatus.UNLOADING);

    private static final List<InvoiceStatus> OUTSTANDING_INVOICE_STATUSES =
            List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

    private static final List<PaymentStatus> PENDING_PAYMENT_STATUSES =
            List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING);

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EnquiryRepository enquiryRepository;
    private final ShipmentRepository shipmentRepository;
    private final QuotationRepository quotationRepository;
    private final NegotiationRepository negotiationRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        BigDecimal outstanding = invoiceRepository.sumOutstandingByClientId(client.getId(), OUTSTANDING_INVOICE_STATUSES);

        return new DashboardSummaryResponse(
                enquiryRepository.countByClientId(client.getId()),
                shipmentRepository.countByClientIdAndStatusIn(client.getId(), ACTIVE_SHIPMENT_STATUSES),
                quotationRepository.countByClientIdAndStatus(client.getId(), QuotationStatus.SENT),
                negotiationRepository.countByEnquiry_ClientIdAndStatus(client.getId(), NegotiationThreadStatus.OPEN),
                paymentRepository.countByClientIdAndStatusIn(client.getId(), PENDING_PAYMENT_STATUSES),
                outstanding == null ? BigDecimal.ZERO : outstanding,
                shipmentRepository.countByClientIdAndStatusIn(client.getId(),
                        List.of(ShipmentStatus.DELIVERED, ShipmentStatus.COMPLETED)));
    }
}
