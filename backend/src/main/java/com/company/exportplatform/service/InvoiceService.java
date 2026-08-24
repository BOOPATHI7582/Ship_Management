package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.InvoiceItemRequest;
import com.company.exportplatform.dto.request.InvoiceRequest;
import com.company.exportplatform.dto.response.InvoiceItemResponse;
import com.company.exportplatform.dto.response.InvoiceResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Invoice;
import com.company.exportplatform.entity.InvoiceItem;
import com.company.exportplatform.entity.ProformaInvoice;
import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.InvoiceStatus;
import com.company.exportplatform.entity.enums.InvoiceType;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.ProformaInvoiceStatus;
import com.company.exportplatform.entity.enums.QuotationStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.InvoiceRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Tax invoice lifecycle: issued immediately on creation (legal document),
 * SENT after dispatch, PAID/PARTIALLY_PAID once the payments phase lands.
 * Corrections are made by cancelling and re-issuing - never by editing.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private static final Set<ProformaInvoiceStatus> PI_INVOICEABLE =
            Set.of(ProformaInvoiceStatus.SENT, ProformaInvoiceStatus.PAYMENT_PENDING,
                    ProformaInvoiceStatus.ADVANCE_PAID);

    /** A final bill closes the chain, so an already-converted proforma stays eligible. */
    private static final Set<ProformaInvoiceStatus> PI_FINALBILL_ELIGIBLE =
            Set.of(ProformaInvoiceStatus.SENT, ProformaInvoiceStatus.PAYMENT_PENDING,
                    ProformaInvoiceStatus.ADVANCE_PAID, ProformaInvoiceStatus.CONVERTED);

    private final InvoiceRepository invoiceRepository;
    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final QuotationRepository quotationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final TaxCalculationService taxCalculationService;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final InvoicePdfService invoicePdfService;
    private final AuditService auditService;

    // ---------- manager side ----------

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> list(String status, String search, Pageable pageable) {
        Specification<Invoice> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), InvoiceStatus.valueOf(status)));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid invoice status");
                }
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("invoiceNo")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("fullName")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("companyName")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return invoiceRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public InvoiceResponse issue(String staffEmail, InvoiceRequest request) {
        User staff = requireUser(staffEmail);
        ProformaInvoice pi = null;
        Quotation quotation;
        InvoiceType docType = parseInvoiceType(request.invoiceType());

        if (request.proformaInvoiceId() != null) {
            pi = proformaInvoiceRepository.findById(request.proformaInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proforma invoice not found"));
            Set<ProformaInvoiceStatus> allowed = docType == InvoiceType.FINAL_BILL
                    ? PI_FINALBILL_ELIGIBLE : PI_INVOICEABLE;
            if (!allowed.contains(pi.getStatus())) {
                throw new BadRequestException(docType == InvoiceType.FINAL_BILL
                        ? "Final bills can be issued against a sent or converted proforma (current: " + pi.getStatus() + ")"
                        : "Tax invoices can be issued against a SENT or advance-paid proforma (current: " + pi.getStatus() + ")");
            }
            quotation = pi.getQuotation();
            if (quotation == null) {
                throw new BadRequestException("Linked proforma invoice has no quotation reference");
            }
        } else if (request.quotationId() != null) {
            if (docType == InvoiceType.FINAL_BILL) {
                throw new BadRequestException(
                        "A final bill must be issued against a proforma invoice so advance payments can be adjusted");
            }
            quotation = quotationRepository.findById(request.quotationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
            if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
                throw new BadRequestException("Direct invoicing requires an ACCEPTED quotation");
            }
        } else {
            throw new BadRequestException("Either proformaInvoiceId or quotationId is required");
        }

        Client client = quotation.getClient();
        if (client == null) {
            throw new BadRequestException("Source document has no client profile");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(documentNumberGenerator.next("INVOICE", "INV"));
        invoice.setClient(client);
        invoice.setQuotation(quotation);
        invoice.setProformaInvoice(pi);
        invoice.setInvoiceType(docType);
        invoice.setIssueDate(LocalDate.now());
        invoice.setCurrency(quotation.getCurrency());
        invoice.setIncoterms(quotation.getIncoterms());

        // legal-address snapshots fall back from quotation -> client profile
        invoice.setBillingAddressLine1(firstNonBlank(quotation.getBillingAddressLine1(), client.getAddressLine1()));
        invoice.setBillingAddressLine2(firstNonBlank(quotation.getBillingAddressLine2(), client.getAddressLine2()));
        invoice.setBillingCity(firstNonBlank(quotation.getBillingCity(), client.getCity()));
        invoice.setBillingState(firstNonBlank(quotation.getBillingState(), client.getState()));
        invoice.setBillingPostalCode(firstNonBlank(quotation.getBillingPostalCode(), client.getPostalCode()));
        invoice.setBillingCountry(firstNonBlank(quotation.getBillingCountry(), client.getCountry()));
        invoice.setShippingAddressLine1(firstNonBlank(quotation.getShippingAddressLine1(), quotation.getBillingAddressLine1()));
        invoice.setShippingAddressLine2(firstNonBlank(quotation.getShippingAddressLine2(), quotation.getBillingAddressLine2()));
        invoice.setShippingCity(firstNonBlank(quotation.getShippingCity(), quotation.getBillingCity()));
        invoice.setShippingState(firstNonBlank(quotation.getShippingState(), quotation.getBillingState()));
        invoice.setShippingPostalCode(firstNonBlank(quotation.getShippingPostalCode(), quotation.getBillingPostalCode()));
        invoice.setShippingCountry(firstNonBlank(quotation.getShippingCountry(), quotation.getBillingCountry()));

        invoice.setGstin(quotation.getGstin() != null && !quotation.getGstin().isBlank()
                ? quotation.getGstin() : client.getGstin());
        invoice.setPan(derivePan(invoice.getGstin()));
        invoice.setPlaceOfSupply(request.placeOfSupply());
        invoice.setExchangeRate(request.exchangeRate());
        invoice.setPortOfLoading(request.portOfLoading());
        invoice.setPortOfDischarge(request.portOfDischarge());
        invoice.setExportReference(request.exportReference());

        computeFinancials(invoice, request);

        invoice.setPaymentTerms(firstNonBlank(request.paymentTerms(),
                firstNonBlank(quotation.getPaymentTerms(), pi != null ? pi.getPaymentTerms() : null)));
        invoice.setBankDetails(firstNonBlank(request.bankDetails(),
                pi != null ? pi.getBankDetails() : null));
        invoice.setNotes(firstNonBlank(request.notes(), pi != null ? pi.getNotes() : null));
        invoice.setTermsConditions(request.termsConditions());
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(invoice.getGrandTotal());
        invoice.setCreatedBy(staff);

        // Final bill: adjust advance already collected on earlier invoices of the
        // same proforma chain so the client is never billed twice for the advance.
        if (docType == InvoiceType.FINAL_BILL) {
            BigDecimal advanceCollected = invoiceRepository
                    .findByProformaInvoiceIdAndStatusNot(pi.getId(), InvoiceStatus.CANCELLED).stream()
                    .map(Invoice::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal applied = advanceCollected.min(invoice.getGrandTotal());
            BigDecimal balance = invoice.getGrandTotal().subtract(applied).max(BigDecimal.ZERO);
            invoice.setPaidAmount(applied);
            invoice.setBalanceAmount(balance);
            if (balance.signum() == 0 && invoice.getGrandTotal().signum() > 0) {
                invoice.setStatus(InvoiceStatus.PAID);
            } else if (applied.signum() > 0) {
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            }
            log.info("Final bill {} adjusts {} advance already collected against {}",
                    invoice.getInvoiceNo(), money(applied, invoice.getCurrency()), pi.getPiNo());
        }

        Invoice saved = invoiceRepository.save(invoice);

        // lifecycle: the sourced proforma is now converted into an invoice
        if (pi != null && pi.getStatus() == ProformaInvoiceStatus.SENT) {
            pi.setStatus(ProformaInvoiceStatus.CONVERTED);
            proformaInvoiceRepository.save(pi);
        }

        log.info("Invoice {} issued from {} by {}",
                saved.getInvoiceNo(),
                pi != null ? pi.getPiNo() : quotation.getQuoteNo(),
                staffEmail);
        auditService.record(staffEmail, "INVOICE_ISSUED", "INVOICE", saved.getId(),
                null, java.util.Map.of("invoiceNo", saved.getInvoiceNo(),
                        "grandTotal", saved.getGrandTotal(),
                        "status", saved.getStatus().name()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse detailForManager(Long id) {
        return toResponse(findInvoice(id));
    }

    @Transactional
    public InvoiceResponse send(Long id, String staffEmail) {
        Invoice invoice = findInvoice(id);
        if (invoice.getSentAt() != null || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Invoice has already been sent or is cancelled");
        }
        invoice.setSentAt(LocalDateTime.now());

        byte[] pdf = invoicePdfService.render(toResponse(invoice));
        Client client = invoice.getClient();
        String email = client != null && client.getUser() != null ? client.getUser().getEmail() : null;
        boolean mailed = false;
        if (email != null && !email.isBlank()) {
            mailed = mailService.sendHtmlWithAttachment(
                    email,
                    "Tax Invoice " + invoice.getInvoiceNo() + " - Global Export",
                    buildEmailBody(invoice),
                    invoice.getInvoiceNo() + ".pdf",
                    pdf,
                    "application/pdf");
        }
        log.info("Invoice {} sent by {} (email delivered: {})", invoice.getInvoiceNo(), staffEmail, mailed);

        if (client != null && client.getUser() != null) {
            notificationService.notify(client.getUser(), NotificationType.INVOICE,
                    "Tax invoice " + invoice.getInvoiceNo() + " received",
                    "Invoice " + invoice.getInvoiceNo() + " for "
                            + money(invoice.getGrandTotal(), invoice.getCurrency())
                            + " is now available in your dashboard.",
                    "/client/invoices/" + invoice.getId(),
                    "INVOICE", invoice.getId());
        }
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse cancel(Long id, String staffEmail) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("A paid or cancelled invoice cannot be cancelled");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        log.info("Invoice {} cancelled by {}", invoice.getInvoiceNo(), staffEmail);
        auditService.record(staffEmail, "INVOICE_CANCELLED", "INVOICE", invoice.getId(),
                null, java.util.Map.of("invoiceNo", invoice.getInvoiceNo()));
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public byte[] pdfForManager(Long id) {
        return invoicePdfService.render(toResponse(findInvoice(id)));
    }

    // ---------- client side ----------

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listMine(String email, Pageable pageable) {
        Client client = requireClient(email);
        return invoiceRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse detailForClient(String email, Long id) {
        return toResponse(ownedInvoice(email, id));
    }

    @Transactional(readOnly = true)
    public byte[] pdfForClient(String email, Long id) {
        return invoicePdfService.render(toResponse(ownedInvoice(email, id)));
    }

    // ---------- helpers ----------

    private void computeFinancials(Invoice invoice, InvoiceRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        invoice.getItems().clear();

        int order = 0;
        for (InvoiceItemRequest itemReq : request.items()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setItemOrder(order++);
            item.setDescription(itemReq.description().trim());
            item.setHsnCode(itemReq.hsnCode() == null || itemReq.hsnCode().isBlank()
                    ? null : itemReq.hsnCode().trim());
            BigDecimal qty = scale(itemReq.quantity());
            BigDecimal rate = scale(itemReq.ratePerUnit());
            item.setQuantity(qty);
            item.setUnit(itemReq.unit() == null || itemReq.unit().isBlank() ? null : itemReq.unit().trim());
            item.setRatePerUnit(rate);
            item.setLineAmount(qty.multiply(rate).setScale(4, RoundingMode.HALF_UP));
            subtotal = subtotal.add(item.getLineAmount());
            invoice.getItems().add(item);
        }
        invoice.setSubtotal(scale(subtotal));

        Quotation quotation = invoice.getQuotation();
        invoice.setDiscount(nzOrOverride(request.discount(), quotation.getDiscount()));
        invoice.setFreightCharges(nzOrOverride(request.freightCharges(), quotation.getFreightCharges()));
        invoice.setLoadingCharges(nzOrOverride(request.loadingCharges(), quotation.getLoadingCharges()));
        invoice.setDocumentationCharges(nzOrOverride(request.documentationCharges(), quotation.getDocumentationCharges()));
        invoice.setInsuranceCharges(nzOrOverride(request.insuranceCharges(), quotation.getInsuranceCharges()));
        invoice.setOtherCharges(nzOrOverride(request.otherCharges(), quotation.getOtherCharges()));
        invoice.setAdditionalCharges(scale(request.additionalCharges()));

        BigDecimal taxable = invoice.getSubtotal()
                .subtract(invoice.getDiscount())
                .add(invoice.getFreightCharges())
                .add(invoice.getLoadingCharges())
                .add(invoice.getDocumentationCharges())
                .add(invoice.getInsuranceCharges())
                .add(invoice.getOtherCharges())
                .add(invoice.getAdditionalCharges())
                .max(BigDecimal.ZERO);
        invoice.setTaxableAmount(scale(taxable));

        String treatmentName = request.taxTreatment();
        if (treatmentName == null || treatmentName.isBlank()) {
            treatmentName = quotation.getTaxTreatment() != null ? quotation.getTaxTreatment().name() : null;
        }
        if (treatmentName != null && !treatmentName.isBlank()) {
            try {
                invoice.setTaxTreatment(com.company.exportplatform.entity.enums.TaxTreatment
                        .valueOf(treatmentName.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unknown tax treatment: " + treatmentName);
            }
        }

        var taxResult = taxCalculationService.compute(
                treatmentName, null, invoice.getBillingCountry(), invoice.getTaxableAmount());
        invoice.setCgstAmount(taxResult.cgstAmount());
        invoice.setSgstAmount(taxResult.sgstAmount());
        invoice.setIgstAmount(taxResult.igstAmount());
        BigDecimal attributed = taxResult.cgstAmount().add(taxResult.sgstAmount()).add(taxResult.igstAmount());
        invoice.setOtherTaxAmount(taxResult.totalTax().subtract(attributed).max(BigDecimal.ZERO));
        invoice.setTotalTaxAmount(taxResult.totalTax());
        invoice.setGrandTotal(scale(invoice.getTaxableAmount().add(taxResult.totalTax())));
        invoice.setDueDate(request.dueDate());
    }

    private String buildEmailBody(Invoice invoice) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:560px\">"
                + "<h2 style=\"color:#0f172a;margin-bottom:4px\">Tax Invoice " + invoice.getInvoiceNo() + "</h2>"
                + "<p style=\"color:#334155\">Dear partner,</p>"
                + "<p style=\"color:#334155\">Please find attached tax invoice <strong>" + invoice.getInvoiceNo()
                + "</strong> amounting to <strong>" + money(invoice.getGrandTotal(), invoice.getCurrency())
                + "</strong>" + (invoice.getDueDate() != null ? ", due by " + invoice.getDueDate() : "")
                + ".</p>"
                + "<p style=\"color:#334155\">Kindly arrange payment through the bank details printed on the invoice.</p>"
                + "<p style=\"color:#64748b;font-size:12px\">Global Export • accounts@globalexport.example</p>"
                + "</div>";
    }

    private String derivePan(String gstin) {
        return gstin != null && gstin.length() >= 12 ? gstin.substring(2, 12) : null;
    }

    private static InvoiceType parseInvoiceType(String raw) {
        if (raw == null || raw.isBlank()) {
            return InvoiceType.TAX_INVOICE;
        }
        try {
            return InvoiceType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown invoice type: " + raw);
        }
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceItemResponse> items = invoice.getItems().stream()
                .map(item -> new InvoiceItemResponse(item.getId(), item.getItemOrder(), item.getDescription(),
                        item.getHsnCode(), item.getQuantity(), item.getUnit(), item.getRatePerUnit(),
                        item.getLineAmount()))
                .toList();
        ProformaInvoice pi = invoice.getProformaInvoice();
        Quotation quotation = invoice.getQuotation();
        Client client = invoice.getClient();
        User clientUser = client != null ? client.getUser() : null;
        return new InvoiceResponse(
                invoice.getId(), invoice.getInvoiceNo(), invoice.getInvoiceType() != null ? invoice.getInvoiceType().name() : null,
                pi != null ? pi.getId() : null, pi != null ? pi.getPiNo() : null,
                quotation != null ? quotation.getId() : null, quotation != null ? quotation.getQuoteNo() : null,
                client != null ? client.getId() : null, displayName(clientUser),
                invoice.getIssueDate(), invoice.getDueDate(),
                invoice.getBillingAddressLine1(), invoice.getBillingAddressLine2(), invoice.getBillingCity(),
                invoice.getBillingState(), invoice.getBillingPostalCode(), invoice.getBillingCountry(),
                invoice.getShippingAddressLine1(), invoice.getShippingAddressLine2(), invoice.getShippingCity(),
                invoice.getShippingState(), invoice.getShippingPostalCode(), invoice.getShippingCountry(),
                invoice.getGstin(), invoice.getPan(), invoice.getPlaceOfSupply(),
                invoice.getCurrency(), invoice.getExchangeRate(), invoice.getIncoterms(),
                invoice.getPortOfLoading(), invoice.getPortOfDischarge(), invoice.getExportReference(),
                invoice.getSubtotal(), invoice.getDiscount(), invoice.getFreightCharges(), invoice.getLoadingCharges(),
                invoice.getDocumentationCharges(), invoice.getInsuranceCharges(), invoice.getOtherCharges(),
                invoice.getAdditionalCharges(), invoice.getTaxableAmount(),
                invoice.getTaxTreatment() != null ? invoice.getTaxTreatment().name() : null,
                invoice.getTaxTreatment() != null ? invoice.getTaxTreatment().name().replace('_', ' ') : null,
                null,
                invoice.getCgstAmount(), invoice.getSgstAmount(), invoice.getIgstAmount(),
                invoice.getOtherTaxAmount(), invoice.getTotalTaxAmount(),
                invoice.getGrandTotal(), invoice.getPaidAmount(), invoice.getBalanceAmount(),
                invoice.getPaymentTerms(), invoice.getBankDetails(), invoice.getNotes(), invoice.getTermsConditions(),
                invoice.getStatus().name(), invoice.getSentAt(), invoice.getCreatedAt(), items);
    }

    private String displayName(User user) {
        if (user == null) {
            return "Client";
        }
        return user.getCompanyName() != null && !user.getCompanyName().isBlank()
                ? user.getCompanyName() : user.getFullName();
    }

    private Invoice ownedInvoice(String email, Long id) {
        Client client = requireClient(email);
        Invoice invoice = findInvoice(id);
        if (invoice.getClient() == null || !client.getId().equals(invoice.getClient().getId())) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        return invoice;
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
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
        return scale(requested != null ? requested : fallback);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        return b != null && !b.isBlank() ? b.trim() : null;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static String money(BigDecimal amount, String currency) {
        return currency + " " + String.format(Locale.ENGLISH, "%,.2f",
                amount == null ? BigDecimal.ZERO : amount);
    }
}
