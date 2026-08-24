package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.QuotationDecisionRequest;
import com.company.exportplatform.dto.request.QuotationItemRequest;
import com.company.exportplatform.dto.request.QuotationRequest;
import com.company.exportplatform.dto.response.ManagerQuotationSummary;
import com.company.exportplatform.dto.response.QuotationItemResponse;
import com.company.exportplatform.dto.response.QuotationResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.Negotiation;
import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.QuotationItem;
import com.company.exportplatform.entity.TaxRate;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.EnquiryStatus;
import com.company.exportplatform.entity.enums.NegotiationThreadStatus;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.QuotationStatus;
import com.company.exportplatform.entity.enums.RoleName;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.NegotiationRepository;
import com.company.exportplatform.repository.QuotationRepository;
import com.company.exportplatform.repository.TaxRateRepository;
import com.company.exportplatform.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.UUID;

/**
 * Quotation lifecycle: DRAFT -> SENT -> VIEWED -> ACCEPTED/REJECTED.
 * All financial totals are computed server-side with BigDecimal; the client
 * never submits amounts of record.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuotationService {

    private static final Set<EnquiryStatus> QUOTABLE = Set.of(
            EnquiryStatus.NEW, EnquiryStatus.REVIEWING, EnquiryStatus.CONTACTED,
            EnquiryStatus.NEGOTIATING, EnquiryStatus.QUOTATION_SENT, EnquiryStatus.APPROVED);

    private static final Set<QuotationStatus> OPEN_QUOTE_STATUSES = Set.of(
            QuotationStatus.DRAFT, QuotationStatus.SENT, QuotationStatus.VIEWED, QuotationStatus.NEGOTIATING);

    private final QuotationRepository quotationRepository;
    private final EnquiryRepository enquiryRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final NegotiationRepository negotiationRepository;
    private final TaxRateRepository taxRateRepository;
    private final TaxCalculationService taxCalculationService;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final QuotationPdfService quotationPdfService;

    // ---------- manager side ----------

    @Transactional(readOnly = true)
    public Page<ManagerQuotationSummary> list(String status, Long enquiryId, Pageable pageable) {
        Specification<Quotation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), QuotationStatus.valueOf(status)));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid quotation status");
                }
            }
            if (enquiryId != null) {
                predicates.add(cb.equal(root.get("enquiry").get("id"), enquiryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return quotationRepository.findAll(spec, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public QuotationResponse detailForManager(Long id) {
        return toResponse(findQuotation(id));
    }

    @Transactional
    public QuotationResponse create(String staffEmail, QuotationRequest request) {
        User staff = requireUser(staffEmail);
        Enquiry enquiry = enquiryRepository.findById(request.enquiryId())
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        if (!QUOTABLE.contains(enquiry.getStatus())) {
            throw new BadRequestException("Cannot create a quotation for a " + enquiry.getStatus() + " enquiry");
        }
        Client client = enquiry.getClient();
        if (client == null) {
            throw new BadRequestException("Enquiry has no linked client profile");
        }

        Quotation quotation = new Quotation();
        quotation.setQuoteNo(documentNumberGenerator.next("QUOTATION", "QUO"));
        quotation.setSecureToken(UUID.randomUUID().toString().replace("-", ""));
        quotation.setEnquiry(enquiry);
        quotation.setClient(client);
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCreatedBy(staff);
        applyRequest(quotation, request, client, enquiry);
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse update(Long id, String staffEmail, QuotationRequest request) {
        requireUser(staffEmail);
        Quotation quotation = findQuotation(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT quotations can be edited. Send a revised quotation instead.");
        }
        applyRequest(quotation, request, quotation.getClient(), quotation.getEnquiry());
        return toResponse(quotation);
    }

    @Transactional
    public QuotationResponse send(Long id, String staffEmail) {
        requireUser(staffEmail);
        Quotation quotation = findQuotation(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT quotations can be sent");
        }

        // Versioning: only one live quote per enquiry - retire every other open one.
        quotationRepository.findByEnquiryId(quotation.getEnquiry().getId()).stream()
                .filter(other -> !other.getId().equals(id))
                .filter(other -> OPEN_QUOTE_STATUSES.contains(other.getStatus()))
                .forEach(other -> other.setStatus(QuotationStatus.CANCELLED));

        quotation.setStatus(QuotationStatus.SENT);
        quotation.setSentAt(LocalDateTime.now());

        Enquiry enquiry = quotation.getEnquiry();
        if (enquiry.getStatus() != EnquiryStatus.APPROVED
                && enquiry.getStatus() != EnquiryStatus.CONVERTED
                && enquiry.getStatus() != EnquiryStatus.CLOSED) {
            enquiry.setStatus(EnquiryStatus.QUOTATION_SENT);
        }

        notifyClient(quotation, "Quotation " + quotation.getQuoteNo() + " received",
                "A quotation for enquiry " + enquiry.getReferenceNo() + " is ready. Grand total: "
                        + quotation.getCurrency() + " " + money(quotation.getGrandTotal()) + ".",
                "/client/enquiries/" + enquiry.getId());
        notifyManagers("Quotation sent", "Quotation " + quotation.getQuoteNo() + " was sent to "
                        + displayName(quotation.getClient()) + " for enquiry " + enquiry.getReferenceNo() + ".",
                "/manager/enquiries/" + enquiry.getId(), quotation.getId());
        emailClientWithPdf(quotation, enquiry);

        return toResponse(quotation);
    }

    // ---------- client side ----------

    @Transactional(readOnly = true)
    public Page<ManagerQuotationSummary> listMine(String email, Pageable pageable) {
        Client client = clientOf(email);
        return quotationRepository.findByClientId(client.getId(), pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<ManagerQuotationSummary> listMineForEnquiry(String email, Long enquiryId) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        Client client = clientOf(email);
        if (enquiry.getClient() == null || !enquiry.getClient().getId().equals(client.getId())) {
            throw new ResourceNotFoundException("Enquiry not found");
        }
        return quotationRepository.findByEnquiryId(enquiryId).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public QuotationResponse viewAsClient(String email, Long id) {
        Quotation quotation = ownedQuotation(email, id);
        markViewed(quotation);
        return toResponse(quotation);
    }

    @Transactional
    public QuotationResponse respond(String email, Long id, QuotationDecisionRequest request) {
        Quotation quotation = ownedQuotation(email, id);
        boolean accept = "ACCEPT".equalsIgnoreCase(request.decision());
        if (quotation.getStatus() != QuotationStatus.SENT && quotation.getStatus() != QuotationStatus.VIEWED) {
            throw new BadRequestException("Only sent quotations can be accepted or rejected");
        }
        Enquiry enquiry = quotation.getEnquiry();

        if (accept) {
            quotation.setStatus(QuotationStatus.ACCEPTED);
            quotation.setAcceptedAt(LocalDateTime.now());
            if (enquiry.getStatus() != EnquiryStatus.CONVERTED && enquiry.getStatus() != EnquiryStatus.CLOSED) {
                enquiry.setStatus(EnquiryStatus.APPROVED);
            }
            negotiationRepository
                    .findFirstByEnquiryIdAndStatusOrderByCreatedAtDesc(enquiry.getId(), NegotiationThreadStatus.OPEN)
                    .ifPresent(this::closeThread);
            notifyManagers("Quotation accepted", displayName(quotation.getClient())
                            + " accepted quotation " + quotation.getQuoteNo()
                            + " (" + enquiry.getReferenceNo() + "). Next step: proforma invoice.",
                    "/manager/enquiries/" + enquiry.getId(), quotation.getId());
            notifyClient(quotation, "Quotation accepted",
                    "You accepted quotation " + quotation.getQuoteNo() + ". A proforma invoice will follow shortly.",
                    "/client/enquiries/" + enquiry.getId());
        } else {
            quotation.setStatus(QuotationStatus.REJECTED);
            quotation.setRejectedAt(LocalDateTime.now());
            quotation.setRejectionReason(trimTo(request.reason(), 500));
            if (enquiry.getStatus() == EnquiryStatus.QUOTATION_SENT) {
                enquiry.setStatus(EnquiryStatus.NEGOTIATING);
            }
            notifyManagers("Quotation rejected", displayName(quotation.getClient())
                            + " rejected quotation " + quotation.getQuoteNo() + " ("
                            + enquiry.getReferenceNo() + ")"
                            + (request.reason() != null && !request.reason().isBlank()
                                    ? ": " + trimTo(request.reason(), 200) : "."),
                    "/manager/enquiries/" + enquiry.getId(), quotation.getId());
            notifyClient(quotation, "Quotation declined",
                    "You declined quotation " + quotation.getQuoteNo()
                            + ". Our team will reach out to discuss revised terms.",
                    "/client/enquiries/" + enquiry.getId());
        }
        return toResponse(quotation);
    }

    // ---------- secure public link ----------

    @Transactional
    public QuotationResponse viewByToken(String token) {
        Quotation quotation = quotationRepository.findBySecureToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        markViewed(quotation);
        return toResponse(quotation);
    }

    @Transactional
    public QuotationResponse viewAsManager(Long id) {
        Quotation quotation = findQuotation(id);
        markViewed(quotation);
        return toResponse(quotation);
    }

    // ---------- PDF ----------

    @Transactional(readOnly = true)
    public byte[] pdfForManager(Long id) {
        return quotationPdfService.render(toResponse(findQuotation(id)));
    }

    @Transactional(readOnly = true)
    public byte[] pdfForClient(String email, Long id) {
        return quotationPdfService.render(toResponse(ownedQuotation(email, id)));
    }

    @Transactional(readOnly = true)
    public byte[] pdfByToken(String token) {
        return quotationPdfService.render(viewByToken(token));
    }

    // ---------- internals ----------

    private void applyRequest(Quotation quotation, QuotationRequest request, Client client, Enquiry enquiry) {
        String currency = request.currency() != null && !request.currency().isBlank()
                ? request.currency().trim().toUpperCase(Locale.ROOT)
                : (enquiry.getCurrency() != null ? enquiry.getCurrency() : "INR");
        quotation.setCurrency(currency);

        if (quotation.getQuotationDate() == null) {
            quotation.setQuotationDate(LocalDate.now());
        }
        quotation.setValidUntil(request.validUntil() != null ? request.validUntil()
                : (quotation.getValidUntil() != null ? quotation.getValidUntil() : LocalDate.now().plusDays(30)));

        quotation.setIncoterms(trimTo(request.incoterms(), 20));
        quotation.setPaymentTerms(trimTo(request.paymentTerms(), 1000));
        quotation.setDeliveryTerms(trimTo(request.deliveryTerms(), 1000));
        quotation.setNotes(trimTo(request.notes(), 4000));
        quotation.setTermsConditions(trimTo(request.termsConditions(), 4000));

        quotation.setBillingAddressLine1(valueOr(request.billingAddressLine1(), client.getAddressLine1()));
        quotation.setBillingAddressLine2(valueOr(request.billingAddressLine2(), client.getAddressLine2()));
        quotation.setBillingCity(valueOr(request.billingCity(), client.getCity()));
        quotation.setBillingState(valueOr(request.billingState(), client.getState()));
        quotation.setBillingPostalCode(valueOr(request.billingPostalCode(), client.getPostalCode()));
        quotation.setBillingCountry(valueOr(request.billingCountry(), client.getCountry()));

        User clientUser = client.getUser();
        quotation.setContactEmail(enquiry.getContactEmail() != null
                ? enquiry.getContactEmail()
                : (clientUser != null ? clientUser.getEmail() : null));
        quotation.setContactPhone(enquiry.getContactPhone() != null
                ? enquiry.getContactPhone()
                : (clientUser != null ? clientUser.getPhone() : null));

        replaceItems(quotation, request.items());

        BigDecimal subtotal = quotation.getItems().stream()
                .map(QuotationItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        quotation.setSubtotal(subtotal);

        BigDecimal discount = nvl(request.discount());
        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot exceed the subtotal");
        }
        quotation.setDiscount(discount);
        quotation.setFreightCharges(nvl(request.freightCharges()));
        quotation.setLoadingCharges(nvl(request.loadingCharges()));
        quotation.setDocumentationCharges(nvl(request.documentationCharges()));
        quotation.setInsuranceCharges(nvl(request.insuranceCharges()));
        quotation.setOtherCharges(nvl(request.otherCharges()));

        BigDecimal taxable = subtotal.subtract(discount)
                .add(quotation.getFreightCharges())
                .add(quotation.getLoadingCharges())
                .add(quotation.getDocumentationCharges())
                .add(quotation.getInsuranceCharges())
                .add(quotation.getOtherCharges())
                .setScale(4, RoundingMode.HALF_UP);

        TaxCalculationService.TaxResult tax = taxCalculationService.compute(
                request.taxTreatment(), request.taxRateId(),
                quotation.getBillingCountry(), taxable);

        quotation.setTaxableAmount(tax.taxableAmount());
        quotation.setTaxTreatment(tax.treatment());
        quotation.setTaxAmount(tax.totalTax());
        quotation.setGrandTotal(taxable.add(tax.totalTax()).setScale(4, RoundingMode.HALF_UP));
        if (request.taxRateId() != null) {
            taxRateRepository.findById(request.taxRateId()).ifPresentOrElse(
                    quotation::setTaxRate,
                    () -> { throw new BadRequestException("Selected tax rate does not exist"); });
        } else {
            quotation.setTaxRate(null);
        }
    }

    private void replaceItems(Quotation quotation, List<QuotationItemRequest> requests) {
        quotation.getItems().clear();
        int order = 0;
        for (QuotationItemRequest itemRequest : requests) {
            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setItemOrder(order++);
            item.setDescription(itemRequest.description().trim());
            item.setQuantity(itemRequest.quantity().setScale(4, RoundingMode.HALF_UP));
            item.setUnit(itemRequest.unit() != null ? itemRequest.unit().trim() : null);
            item.setRatePerUnit(itemRequest.ratePerUnit().setScale(4, RoundingMode.HALF_UP));
            item.setLineAmount(item.getQuantity().multiply(item.getRatePerUnit()).setScale(4, RoundingMode.HALF_UP));
            quotation.getItems().add(item);
        }
    }

    private void closeThread(Negotiation thread) {
        thread.setStatus(NegotiationThreadStatus.CLOSED);
        thread.setClosedAt(LocalDateTime.now());
    }

    private void markViewed(Quotation quotation) {
        if (quotation.getStatus() == QuotationStatus.SENT) {
            quotation.setStatus(QuotationStatus.VIEWED);
            quotation.setViewedAt(LocalDateTime.now());
        }
    }

    private void emailClientWithPdf(Quotation quotation, Enquiry enquiry) {
        try {
            String body = "<div style=\"font-family:Arial,sans-serif;font-size:14px;color:#0f172a\">"
                    + "<p>Dear " + displayName(quotation.getClient()) + ",</p>"
                    + "<p>Please find attached our quotation <b>" + quotation.getQuoteNo()
                    + "</b> for your enquiry <b>" + enquiry.getReferenceNo() + "</b>.</p>"
                    + "<p>Grand total: <b>" + quotation.getCurrency() + " " + money(quotation.getGrandTotal()) + "</b>"
                    + (quotation.getValidUntil() != null
                            ? " &nbsp;(valid until " + DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                                    .format(quotation.getValidUntil()) + ")" : "")
                    + "</p>"
                    + "<p>You can review and respond online at <b>/quotation/" + quotation.getSecureToken() + "</b>.</p>"
                    + "<p>Warm regards,<br/>Global Export Operations Team</p></div>";
            byte[] pdf = quotationPdfService.render(toResponse(quotation));
            mailService.sendHtmlWithAttachment(quotation.getContactEmail(),
                    "Quotation " + quotation.getQuoteNo() + " - " + enquiry.getReferenceNo(),
                    body, quotation.getQuoteNo() + ".pdf", pdf, "application/pdf");
        } catch (Exception ex) {
            log.warn("Quotation {} email/PDF step skipped: {}", quotation.getQuoteNo(), ex.getMessage());
        }
    }

    private Quotation ownedQuotation(String email, Long id) {
        return quotationRepository.findByIdAndClient_User_EmailIgnoreCase(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
    }

    private Quotation findQuotation(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
    }

    private Client clientOf(String email) {
        User user = requireUser(email);
        return clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ManagerQuotationSummary toSummary(Quotation quotation) {
        Client client = quotation.getClient();
        User clientUser = client != null ? client.getUser() : null;
        Enquiry enquiry = quotation.getEnquiry();
        return new ManagerQuotationSummary(
                quotation.getId(),
                quotation.getQuoteNo(),
                enquiry != null ? enquiry.getId() : null,
                enquiry != null ? enquiry.getReferenceNo() : null,
                displayName(client),
                clientUser != null ? clientUser.getEmail() : null,
                quotation.getValidUntil(),
                quotation.getCurrency(),
                quotation.getGrandTotal(),
                quotation.getStatus() != null ? quotation.getStatus().name() : null,
                quotation.getSentAt(),
                quotation.getCreatedAt()
        );
    }

    private QuotationResponse toResponse(Quotation q) {
        TaxRate rate = q.getTaxRate();
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        String rateName = rate != null ? rate.getName() : null;
        BigDecimal ratePercent = rate != null ? scale(rate.getRate()) : null;
        try {
            TaxCalculationService.TaxResult breakdown = taxCalculationService.compute(
                    q.getTaxTreatment() != null ? q.getTaxTreatment().name() : null,
                    rate != null ? rate.getId() : null,
                    q.getBillingCountry(),
                    q.getTaxableAmount());
            cgst = breakdown.cgstAmount();
            sgst = breakdown.sgstAmount();
            igst = breakdown.igstAmount();
            if (rate == null) {
                rateName = breakdown.rateName();
                ratePercent = breakdown.ratePercent();
            }
        } catch (Exception ex) {
            log.debug("Tax display breakdown unavailable for {}: {}", q.getQuoteNo(), ex.getMessage());
        }

        List<QuotationItemResponse> items = q.getItems().stream()
                .map(item -> new QuotationItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnit(),
                        item.getRatePerUnit(),
                        item.getLineAmount()))
                .toList();

        Client client = q.getClient();
        User clientUser = client != null ? client.getUser() : null;
        Enquiry enquiry = q.getEnquiry();

        return new QuotationResponse(
                q.getId(),
                q.getQuoteNo(),
                enquiry != null ? enquiry.getId() : null,
                enquiry != null ? enquiry.getReferenceNo() : null,
                client != null ? client.getId() : null,
                displayName(client),
                q.getQuotationDate(),
                q.getValidUntil(),
                q.getCurrency(),
                q.getIncoterms(),
                q.getPaymentTerms(),
                q.getDeliveryTerms(),
                q.getNotes(),
                q.getTermsConditions(),
                q.getBillingAddressLine1(),
                q.getBillingAddressLine2(),
                q.getBillingCity(),
                q.getBillingState(),
                q.getBillingPostalCode(),
                q.getBillingCountry(),
                q.getContactEmail(),
                q.getContactPhone(),
                q.getSubtotal(),
                q.getDiscount(),
                q.getFreightCharges(),
                q.getLoadingCharges(),
                q.getDocumentationCharges(),
                q.getInsuranceCharges(),
                q.getOtherCharges(),
                q.getTaxableAmount(),
                q.getTaxTreatment() != null ? q.getTaxTreatment().name() : null,
                rateName,
                ratePercent,
                q.getTaxAmount(),
                scale(cgst),
                scale(sgst),
                scale(igst),
                q.getGrandTotal(),
                q.getStatus() != null ? q.getStatus().name() : null,
                q.getSecureToken(),
                q.getSentAt(),
                q.getViewedAt(),
                q.getAcceptedAt(),
                q.getRejectedAt(),
                q.getRejectionReason(),
                q.getCreatedAt(),
                items
        );
    }

    private void notifyClient(Quotation quotation, String title, String message, String link) {
        Client client = quotation.getClient();
        User user = client != null ? client.getUser() : null;
        if (user != null) {
            notificationService.notify(user, NotificationType.QUOTATION, title,
                    trimTo(message, 990), link, "QUOTATION", quotation.getId());
        }
    }

    private void notifyManagers(String title, String message, String link, Long entityId) {
        userRepository.findByRoleName(RoleName.ADMIN, PageRequest.of(0, 20)).forEach(admin ->
                notificationService.notify(admin, NotificationType.QUOTATION, title,
                        trimTo(message, 990), link, "QUOTATION", entityId));
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

    private static String money(BigDecimal value) {
        return value == null ? "0" : String.format(Locale.ENGLISH, "%,.2f", value);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static String valueOr(String override, String fallback) {
        return override != null && !override.isBlank() ? override.trim() : fallback;
    }

    private static String trimTo(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }
}
