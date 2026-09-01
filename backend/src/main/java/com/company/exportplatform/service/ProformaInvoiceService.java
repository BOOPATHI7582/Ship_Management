package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ProformaItemRequest;
import com.company.exportplatform.dto.request.ProformaRequest;
import com.company.exportplatform.dto.response.ProformaItemResponse;
import com.company.exportplatform.dto.response.ProformaResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.ProformaInvoice;
import com.company.exportplatform.entity.ProformaInvoiceItem;
import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.QuotationItem;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.ProformaInvoiceStatus;
import com.company.exportplatform.entity.enums.QuotationStatus;
import com.company.exportplatform.entity.enums.TaxTreatment;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.ProformaInvoiceRepository;
import com.company.exportplatform.repository.QuotationRepository;
import com.company.exportplatform.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Proforma invoice lifecycle: DRAFT -> SENT (-> PAYMENT_PENDING etc. once the
 * payments phase lands). A PI mirrors an ACCEPTED quotation; totals are always
 * recomputed server-side with BigDecimal.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProformaInvoiceService {

    private static final Set<ProformaInvoiceStatus> EDITABLE = Set.of(ProformaInvoiceStatus.DRAFT);

    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final QuotationRepository quotationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final TaxCalculationService taxCalculationService;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final ProformaPdfService proformaPdfService;

    // ---------- manager side ----------

    @Transactional(readOnly = true)
    public Page<ProformaResponse> list(String status, Long quotationId, String search, Pageable pageable) {
        Specification<ProformaInvoice> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), ProformaInvoiceStatus.valueOf(status)));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid proforma invoice status");
                }
            }
            if (quotationId != null) {
                predicates.add(cb.equal(root.get("quotation").get("id"), quotationId));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("piNo")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("fullName")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("companyName")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return proformaInvoiceRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public ProformaResponse create(String staffEmail, ProformaRequest request) {
        User staff = requireUser(staffEmail);
        Quotation quotation = findQuotation(request.quotationId());
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new BadRequestException(
                    "A proforma invoice can only be generated from an ACCEPTED quotation (current: "
                            + quotation.getStatus() + ")");
        }
        ProformaInvoice pi = new ProformaInvoice();
        pi.setPiNo(documentNumberGenerator.next("PROFORMA", "PI"));
        pi.setQuotation(quotation);
        pi.setClient(quotation.getClient());
        pi.setIssueDate(LocalDate.now());
        applyEditableFields(pi, request, quotation);
        pi.setStatus(ProformaInvoiceStatus.DRAFT);
        pi.setCreatedBy(staff);
        ProformaInvoice saved = proformaInvoiceRepository.save(pi);
        log.info("Proforma {} created from quotation {} by {}", saved.getPiNo(), quotation.getQuoteNo(), staffEmail);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProformaResponse detailForManager(Long id) {
        return toResponse(findPi(id));
    }

    @Transactional
    public ProformaResponse update(Long id, ProformaRequest request) {
        ProformaInvoice pi = findPi(id);
        if (!EDITABLE.contains(pi.getStatus())) {
            throw new BadRequestException("Only DRAFT proforma invoices can be edited");
        }
        if (request.quotationId() != null
                && (pi.getQuotation() == null || !request.quotationId().equals(pi.getQuotation().getId()))) {
            Quotation quotation = findQuotation(request.quotationId());
            if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
                throw new BadRequestException("Linked quotation must be ACCEPTED");
            }
            pi.setQuotation(quotation);
        }
        applyEditableFields(pi, request, pi.getQuotation());
        return toResponse(proformaInvoiceRepository.save(pi));
    }

    @Transactional
    public ProformaResponse send(Long id, String staffEmail) {
        ProformaInvoice pi = findPi(id);
        if (pi.getStatus() != ProformaInvoiceStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT proforma invoices can be sent");
        }
        pi.setStatus(ProformaInvoiceStatus.SENT);
        pi.setSentAt(LocalDateTime.now());

        byte[] pdf = proformaPdfService.render(toResponse(pi));
        Client client = pi.getClient();
        String email = client != null && client.getUser() != null ? client.getUser().getEmail() : null;
        boolean mailed = false;
        if (email != null && !email.isBlank()) {
            mailed = mailService.sendHtmlWithAttachment(
                    email,
                    "Proforma Invoice " + pi.getPiNo() + " - ExportPlatform",
                    buildPiEmailBody(pi),
                    pi.getPiNo() + ".pdf",
                    pdf,
                    "application/pdf");
        }
        log.info("Proforma {} sent by {} (email delivered: {})", pi.getPiNo(), staffEmail, mailed);

        if (client != null && client.getUser() != null) {
            notificationService.notify(client.getUser(), NotificationType.PROFORMA_INVOICE,
                    "Proforma invoice " + pi.getPiNo() + " received",
                    "Advance payment proforma " + pi.getPiNo()
                            + " for " + money(pi.getGrandTotal(), pi.getCurrency()) + " is awaiting your confirmation.",
                    "/client/proforma-invoices/" + pi.getId(),
                    "PROFORMA_INVOICE", pi.getId());
        }
        return toResponse(pi);
    }

    @Transactional
    public ProformaResponse cancel(Long id, String staffEmail) {
        ProformaInvoice pi = findPi(id);
        if (pi.getStatus() == ProformaInvoiceStatus.CONVERTED
                || pi.getStatus() == ProformaInvoiceStatus.CANCELLED) {
            throw new BadRequestException("Proforma invoice is already " + pi.getStatus());
        }
        pi.setStatus(ProformaInvoiceStatus.CANCELLED);
        log.info("Proforma {} cancelled by {}", pi.getPiNo(), staffEmail);
        return toResponse(pi);
    }

    @Transactional(readOnly = true)
    public byte[] pdfForManager(Long id) {
        return proformaPdfService.render(toResponse(findPi(id)));
    }

    // ---------- client side ----------

    @Transactional(readOnly = true)
    public Page<ProformaResponse> listMine(String email, Pageable pageable) {
        Client client = requireClient(email);
        return proformaInvoiceRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProformaResponse detailForClient(String email, Long id) {
        ProformaInvoice pi = ownedPi(email, id);
        return toResponse(pi);
    }

    @Transactional(readOnly = true)
    public byte[] pdfForClient(String email, Long id) {
        return proformaPdfService.render(toResponse(ownedPi(email, id)));
    }

    // ---------- helpers ----------

    private void applyEditableFields(ProformaInvoice pi, ProformaRequest request, Quotation quotation) {
        boolean fromQuote = quotation != null;

        BigDecimal subtotal = BigDecimal.ZERO;
        pi.getItems().clear();

        List<ProformaItemRequest> items = request.items();
        int order = 0;
        for (ProformaItemRequest itemReq : items) {
            ProformaInvoiceItem item = new ProformaInvoiceItem();
            item.setProformaInvoice(pi);
            item.setItemOrder(order++);
            item.setDescription(itemReq.description().trim());
            BigDecimal qty = scale(itemReq.quantity());
            BigDecimal rate = scale(itemReq.ratePerUnit());
            item.setQuantity(qty);
            item.setUnit(itemReq.unit() == null || itemReq.unit().isBlank()
                    ? (fromQuote ? "" : "") : itemReq.unit().trim());
            item.setRatePerUnit(rate);
            item.setLineAmount(qty.multiply(rate).setScale(4, RoundingMode.HALF_UP));
            subtotal = subtotal.add(item.getLineAmount());
            pi.getItems().add(item);
        }
        pi.setSubtotal(scale(subtotal));

        pi.setDiscount(nzOrOverride(request.discount(),
                fromQuote ? quotation.getDiscount() : BigDecimal.ZERO));
        pi.setCurrency(fromQuote ? quotation.getCurrency()
                : (pi.getCurrency() == null ? "INR" : pi.getCurrency()));

        BigDecimal taxable = pi.getSubtotal()
                .subtract(pi.getDiscount())
                .add(nzOrOverride(request.freightCharges(), BigDecimal.ZERO))
                .add(nzOrOverride(request.loadingCharges(), BigDecimal.ZERO))
                .add(nzOrOverride(request.documentationCharges(), BigDecimal.ZERO))
                .add(nzOrOverride(request.insuranceCharges(), BigDecimal.ZERO))
                .add(nzOrOverride(request.otherCharges(), BigDecimal.ZERO))
                .max(BigDecimal.ZERO);
        pi.setTaxableAmount(scale(taxable));

        TaxTreatment treatment = resolveTreatment(request.taxTreatment(),
                fromQuote ? quotation.getTaxTreatment() : null);
        pi.setTaxTreatment(treatment);
        Long taxRateId = request.taxRateId() != null ? request.taxRateId()
                : (fromQuote && quotation.getTaxRate() != null ? quotation.getTaxRate().getId() : null);
        String country = fromQuote
                ? (quotation.getCountry() != null ? quotation.getCountry() : quotation.getBillingCountry())
                : null;
        var taxResult = taxCalculationService.compute(
                treatment == null ? null : treatment.name(), taxRateId, country, pi.getTaxableAmount());
        pi.setTaxAmount(taxResult.totalTax());

        BigDecimal grandTotal = pi.getTaxableAmount().add(pi.getTaxAmount());
        pi.setGrandTotal(scale(grandTotal));

        pi.setValidUntil(request.validUntil() != null ? request.validUntil()
                : (fromQuote ? quotation.getValidUntil() : LocalDate.now().plusDays(30)));

        pi.setPaymentTerms(firstNonBlank(request.paymentTerms(),
                fromQuote ? quotation.getPaymentTerms() : null));
        if (request.bankDetails() != null) {
            pi.setBankDetails(request.bankDetails().isBlank() ? pi.getBankDetails() : request.bankDetails());
        } else if (pi.getBankDetails() == null) {
            pi.setBankDetails(DEFAULT_BANK_DETAILS);
        }
        if (request.notes() != null) {
            pi.setNotes(request.notes().isBlank() ? pi.getNotes() : request.notes());
        } else if (fromQuote && quotation.getNotes() != null) {
            pi.setNotes(quotation.getNotes());
        }
    }

    private TaxTreatment resolveTreatment(String requested, TaxTreatment fallback) {
        if (requested != null && !requested.isBlank()) {
            for (TaxTreatment t : TaxTreatment.values()) {
                if (t.name().equalsIgnoreCase(requested.trim())) {
                    return t;
                }
            }
            throw new BadRequestException("Unknown tax treatment: " + requested);
        }
        return fallback != null ? fallback : TaxTreatment.EXEMPT;
    }

    private String buildPiEmailBody(ProformaInvoice pi) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:560px\">"
                + "<h2 style=\"color:#0f172a;margin-bottom:4px\">Proforma Invoice " + pi.getPiNo() + "</h2>"
                + "<p style=\"color:#334155\">Dear partner,</p>"
                + "<p style=\"color:#334155\">Please find attached the proforma invoice "
                + "<strong>" + pi.getPiNo() + "</strong> for your advance payment of <strong>"
                + money(pi.getGrandTotal(), pi.getCurrency()) + "</strong>.</p>"
                + "<p style=\"color:#334155\">Bank remittance details are included in the attachment. "
                + "Once the advance is credited, our operations team will confirm shipment booking.</p>"
                + "<p style=\"color:#64748b;font-size:12px\">ExportPlatform • accounts@exportplatform.example</p>"
                + "</div>";
    }

    private ProformaResponse toResponse(ProformaInvoice pi) {
        Quotation quotation = pi.getQuotation();
        List<ProformaItemResponse> items = pi.getItems().stream()
                .map(item -> new ProformaItemResponse(item.getId(), item.getItemOrder(), item.getDescription(),
                        item.getQuantity(), item.getUnit(), item.getRatePerUnit(), item.getLineAmount()))
                .toList();
        return new ProformaResponse(
                pi.getId(), pi.getPiNo(),
                quotation != null ? quotation.getId() : null,
                quotation != null ? quotation.getQuoteNo() : null,
                quotation != null && quotation.getEnquiry() != null ? quotation.getEnquiry().getId() : null,
                quotation != null && quotation.getEnquiry() != null ? quotation.getEnquiry().getReferenceNo() : null,
                pi.getClient() != null ? pi.getClient().getId() : null,
                displayName(pi.getClient()),
                pi.getIssueDate(), pi.getValidUntil(), pi.getCurrency(),
                pi.getSubtotal(), pi.getDiscount(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                pi.getTaxableAmount(),
                pi.getTaxTreatment() != null ? pi.getTaxTreatment().name() : null,
                pi.getTaxTreatment() != null ? pi.getTaxTreatment().name().replace('_', ' ') : null,
                null,
                pi.getTaxAmount(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                pi.getGrandTotal(),
                pi.getPaymentTerms(), pi.getBankDetails(), pi.getNotes(),
                pi.getStatus().name(), pi.getSentAt(), pi.getCreatedAt(), items);
    }

    private ProformaInvoice ownedPi(String email, Long id) {
        Client client = requireClient(email);
        ProformaInvoice pi = findPi(id);
        if (pi.getClient() == null || !client.getId().equals(pi.getClient().getId())) {
            throw new ResourceNotFoundException("Proforma invoice not found");
        }
        return pi;
    }

    private ProformaInvoice findPi(Long id) {
        return proformaInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma invoice not found"));
    }

    private String displayName(Client client) {
        if (client == null || client.getUser() == null) {
            return "Client";
        }
        User user = client.getUser();
        return user.getCompanyName() != null && !user.getCompanyName().isBlank()
                ? user.getCompanyName()
                : user.getFullName();
    }

    private Quotation findQuotation(Long id) {
        if (id == null) {
            throw new BadRequestException("quotationId is required");
        }
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Client requireClient(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
    }

    private static BigDecimal nzOrOverride(BigDecimal requested, BigDecimal fallback) {
        if (requested != null) {
            return scale(requested);
        }
        return scale(fallback);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        return b != null ? b.trim() : null;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static String money(BigDecimal amount, String currency) {
        return currency + " " + String.format(Locale.ENGLISH, "%,.2f",
                amount == null ? BigDecimal.ZERO : amount);
    }

    private static final String DEFAULT_BANK_DETAILS =
            "ExportPlatform Pvt. Ltd.\nHDFC Bank, Fort Branch, Mumbai\n"
                    + "A/C 50200012345678 • IFSC HDFC0000123 • SWIFT HDFCINBB\n"
                    + "Purpose code: P0103 (Export of goods)";
}
