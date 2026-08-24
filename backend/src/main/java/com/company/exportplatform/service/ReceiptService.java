package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.ReceiptResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Invoice;
import com.company.exportplatform.entity.Payment;
import com.company.exportplatform.entity.Receipt;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.ReceiptRepository;
import com.company.exportplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Payment receipts: exactly one REC document per settled payment, issued
 * inside the caller's transaction (capture or offline settlement), emailed
 * to the client with the PDF attached, and exposed read-only afterwards.
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final ReceiptRepository receiptRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final ReceiptPdfService receiptPdfService;
    private final MailService mailService;

    public ReceiptService(ReceiptRepository receiptRepository,
                          ClientRepository clientRepository,
                          UserRepository userRepository,
                          DocumentNumberGenerator documentNumberGenerator,
                          ReceiptPdfService receiptPdfService,
                          MailService mailService) {
        this.receiptRepository = receiptRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.documentNumberGenerator = documentNumberGenerator;
        this.receiptPdfService = receiptPdfService;
        this.mailService = mailService;
    }

    // ---------------------------------------------------------------- issuance

    /**
     * Called from {@link PaymentService} inside its transaction after a payment
     * is settled. Idempotent: a payment can never produce two receipts.
     */
    @Transactional
    public void issueForPayment(Payment payment) {
        if (payment == null || payment.getId() == null) {
            return;
        }
        if (receiptRepository.findByPaymentId(payment.getId()).isPresent()) {
            log.debug("Receipt already exists for payment {} - skipping", payment.getId());
            return;
        }

        Receipt receipt = new Receipt();
        receipt.setReceiptNo(documentNumberGenerator.next("RECEIPT", "REC"));
        receipt.setPayment(payment);
        receipt.setClient(payment.getClient());
        receipt.setInvoice(payment.getInvoice());
        receipt.setIssuedOn(java.time.LocalDate.now());
        receipt.setAmount(PaymentService.nvl(payment.getAmount()));
        receipt.setCurrency(payment.getCurrency() != null ? payment.getCurrency() : "INR");
        receipt.setMethod(payment.getMethod());
        receipt.setGatewayTransactionId(payment.getTransactionReference());
        Invoice invoice = payment.getInvoice();
        receipt.setRemainingBalance(invoice != null ? PaymentService.nvl(invoice.getBalanceAmount())
                : BigDecimal.ZERO);
        receipt.setNotes(payment.getNotes());
        receiptRepository.save(receipt);

        log.info("Receipt {} issued for payment {} ({})", receipt.getReceiptNo(), payment.getId(),
                receipt.getAmount());
        deliverEmail(receipt);
    }

    /** Email never breaks the payment flow (MailService is failure-safe). */
    private void deliverEmail(Receipt receipt) {
        try {
            User user = receipt.getClient().getUser();
            String company = user.getCompanyName() != null && !user.getCompanyName().isBlank()
                    ? user.getCompanyName()
                    : user.getFullName();
            byte[] pdf = receiptPdfService.render(toResponse(receipt));
            mailService.sendHtmlWithAttachment(
                    user.getEmail(),
                    "Payment received - Receipt " + receipt.getReceiptNo(),
                    """
                            <p>Dear %s,</p>
                            <p>Thank you for your payment. We confirm receipt of
                            <strong>%s %s</strong>%s.</p>
                            <p>Receipt number: <strong>%s</strong><br/>
                            The signed copy is attached to this email and always available in your dashboard.</p>
                            <p>Global Export Pvt. Ltd. — Accounts</p>
                            """.formatted(
                            company,
                            receipt.getCurrency(),
                            PaymentService.nvl(receipt.getAmount()).toPlainString(),
                            receipt.getInvoice() != null
                                    ? " against tax invoice " + receipt.getInvoice().getInvoiceNo()
                                    : "",
                            receipt.getReceiptNo()),
                    receipt.getReceiptNo() + ".pdf",
                    pdf,
                    "application/pdf");
        } catch (Exception ex) {
            log.error("Receipt email failed for {}: {}", receipt.getReceiptNo(), ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- client

    @Transactional(readOnly = true)
    public Page<ReceiptResponse> listMine(String email, Pageable pageable) {
        Client client = ownedClient(email);
        if (client == null) {
            return Page.empty(pageable);
        }
        return receiptRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse detailForClient(String email, Long id) {
        return toResponse(ownedReceipt(email, id));
    }

    @Transactional(readOnly = true)
    public byte[] pdfForClient(String email, Long id) {
        return receiptPdfService.render(toResponse(ownedReceipt(email, id)));
    }

    // ---------------------------------------------------------------- manager / admin

    @Transactional(readOnly = true)
    public Page<ReceiptResponse> listAll(Pageable pageable) {
        return receiptRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getForManager(Long id) {
        return toResponse(receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found")));
    }

    @Transactional(readOnly = true)
    public byte[] pdfForManager(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        return receiptPdfService.render(toResponse(receipt));
    }

    // ---------------------------------------------------------------- internals

    private Client ownedClient(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return clientRepository.findByUserId(user.getId()).orElse(null);
    }

    private Receipt ownedReceipt(String email, Long id) {
        Client client = ownedClient(email);
        if (client == null) {
            throw new ResourceNotFoundException("Receipt not found");
        }
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        if (!receipt.getClient().getId().equals(client.getId())) {
            throw new ResourceNotFoundException("Receipt not found");
        }
        return receipt;
    }

    private ReceiptResponse toResponse(Receipt r) {
        Payment p = r.getPayment();
        Invoice inv = r.getInvoice();
        Client client = r.getClient();
        var user = client != null ? client.getUser() : null;
        return new ReceiptResponse(
                r.getId(),
                r.getReceiptNo(),
                p != null ? p.getId() : null,
                inv != null ? inv.getId() : null,
                inv != null ? inv.getInvoiceNo() : null,
                p != null && p.getProformaInvoice() != null ? p.getProformaInvoice().getId() : null,
                p != null && p.getProformaInvoice() != null ? p.getProformaInvoice().getPiNo() : null,
                client != null ? client.getId() : null,
                user != null ? user.getFullName() : null,
                user != null ? user.getCompanyName() : null,
                r.getIssuedOn(),
                r.getAmount(),
                r.getCurrency(),
                r.getMethod(),
                r.getGatewayTransactionId(),
                r.getRemainingBalance(),
                r.getNotes(),
                r.getCreatedAt());
    }
}
