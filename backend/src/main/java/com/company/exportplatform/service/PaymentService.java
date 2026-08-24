package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.CreateOrderRequest;
import com.company.exportplatform.dto.request.OfflinePaymentRequest;
import com.company.exportplatform.dto.request.VerifyPaymentRequest;
import com.company.exportplatform.dto.response.CreateOrderResponse;
import com.company.exportplatform.dto.response.PaymentResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Invoice;
import com.company.exportplatform.entity.Payment;
import com.company.exportplatform.entity.PaymentTransaction;
import com.company.exportplatform.entity.PaymentWebhook;
import com.company.exportplatform.entity.ProformaInvoice;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.InvoiceStatus;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.PaymentMethod;
import com.company.exportplatform.entity.enums.PaymentStatus;
import com.company.exportplatform.entity.enums.PaymentType;
import com.company.exportplatform.entity.enums.ProformaInvoiceStatus;
import com.company.exportplatform.entity.enums.TransactionStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.InvoiceRepository;
import com.company.exportplatform.repository.PaymentRepository;
import com.company.exportplatform.repository.PaymentTransactionRepository;
import com.company.exportplatform.repository.PaymentWebhookRepository;
import com.company.exportplatform.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Payments: Razorpay checkout flow with a deterministic MOCK gateway fallback
 * (used when no keys are configured) that exercises the identical HMAC verify
 * path, offline payment recording for admins, and an idempotent signature-
 * verified webhook receiver.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentWebhookRepository webhookRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ReceiptService receiptService;
    private final ObjectMapper objectMapper;

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentTransactionRepository transactionRepository,
                          PaymentWebhookRepository webhookRepository,
                          InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          ReceiptService receiptService,
                          ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.webhookRepository = webhookRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.receiptService = receiptService;
        this.objectMapper = objectMapper;
    }

    public boolean isMockMode() {
        return keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank();
    }

    private String verifySecret() {
        return isMockMode() ? "mock-verify-secret" : keySecret;
    }

    // ---------------------------------------------------------------- create order

    @Transactional
    public CreateOrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (!invoice.getClient().getId().equals(client.getId())) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay a cancelled invoice");
        }
        BigDecimal balance = nvl(invoice.getBalanceAmount());
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        String orderId = isMockMode()
                ? "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                : createRazorpayOrder(invoice, balance);

        Payment payment = new Payment();
        payment.setClient(invoice.getClient());
        payment.setInvoice(invoice);
        payment.setProformaInvoice(invoice.getProformaInvoice());
        payment.setPaymentType(nvl(invoice.getPaidAmount()).signum() > 0 ? PaymentType.BALANCE : PaymentType.ADVANCE);
        payment.setMethod(PaymentMethod.RAZORPAY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(balance);
        payment.setCurrency(invoice.getCurrency());
        payment.setRazorpayOrderId(orderId);
        paymentRepository.save(payment);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setPayment(payment);
        tx.setRazorpayOrderId(orderId);
        tx.setAmount(balance);
        tx.setCurrency(invoice.getCurrency());
        tx.setStatus(TransactionStatus.CREATED);
        transactionRepository.save(tx);

        log.info("Created {} order {} for {} ({})", isMockMode() ? "MOCK" : "RAZORPAY",
                orderId, invoice.getInvoiceNo(), balance);

        String mockPaymentId = isMockMode()
                ? "pay_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14)
                : null;

        return new CreateOrderResponse(
                payment.getId(), orderId, balance, invoice.getCurrency(),
                isMockMode() ? null : keyId,
                mockPaymentId,
                // Dev-only: pre-sign so the mock panel can run the identical HMAC verify handshake.
                isMockMode() ? signMock(orderId, mockPaymentId) : null);
    }

    private String createRazorpayOrder(Invoice invoice, BigDecimal amountMinorBase) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", toMinorUnits(amountMinorBase));
            orderRequest.put("currency", invoice.getCurrency());
            orderRequest.put("receipt", invoice.getInvoiceNo());
            orderRequest.put("payment_capture", 1);
            Order order = client.orders.create(orderRequest);
            return order.get("id");
        } catch (Exception e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new BadRequestException("Gateway error while creating order");
        }
    }

    /** Pre-sign a mock payment id so the dev UI can complete the real verify handshake. */
    public String signMock(String orderId, String paymentId) {
        return hmacSha256Hex(orderId + "|" + paymentId, verifySecret());
    }

    // ---------------------------------------------------------------- verify (checkout callback)

    @Transactional
    public PaymentResponse verify(String email, VerifyPaymentRequest request) {
        PaymentTransaction tx = transactionRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Unknown order"));
        Payment payment = tx.getPayment();

        if (!payment.getClient().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResourceNotFoundException("Unknown order");
        }
        if (tx.getStatus() == TransactionStatus.CAPTURED) {
            return toResponse(payment); // idempotent replay
        }

        String expected = hmacSha256Hex(request.razorpayOrderId() + "|" + request.razorpayPaymentId(), verifySecret());
        if (!constantTimeEquals(expected, request.razorpaySignature())) {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setErrorDescription("Signature verification failed");
            payment.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            paymentRepository.save(payment);
            throw new BadRequestException("Invalid payment signature");
        }

        capture(tx, payment, request.razorpayPaymentId(), request.razorpaySignature(), null);
        return toResponse(payment);
    }

    // ---------------------------------------------------------------- webhook

    /**
     * Public endpoint. Verifies the X-Razorpay-Signature header against the raw
     * body using the webhook secret; stores the event keyed by event_id for
     * idempotency and applies captured payments.
     */
    @Transactional
    public String handleWebhook(String rawBody, String signatureHeader) {
        String secret = (webhookSecret != null && !webhookSecret.isBlank()) ? webhookSecret : verifySecret();
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new BadRequestException("Missing signature header");
        }
        String expected = hmacSha256Hex(rawBody, secret);
        if (!constantTimeEquals(expected, signatureHeader)) {
            log.warn("Webhook signature mismatch");
            throw new BadRequestException("Invalid webhook signature");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new BadRequestException("Malformed webhook payload");
        }
        String eventType = root.path("event").asText(null);

        JsonNode entityNode = root.path("payload").path("payment").path("entity");
        String eventId = entityNode.path("id").asText("");
        if (eventId.isBlank()) {
            eventId = "evt_" + UUID.randomUUID();
        }

        PaymentWebhook hook = new PaymentWebhook();
        hook.setEventId(eventId);
        hook.setEventType(eventType);
        hook.setPayload(rawBody.length() > 10000 ? rawBody.substring(0, 10000) : rawBody);
        hook.setSignature(signatureHeader);
        hook.setReceivedAt(LocalDateTime.now());
        try {
            webhookRepository.save(hook);
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            log.info("Duplicate webhook event {} skipped", eventId);
            return "DUPLICATE_SKIPPED";
        }

        boolean relevant = eventType != null && (
                eventType.equals("payment.captured") || eventType.equals("payment.authorized"));
        if (!relevant) {
            hook.setProcessed(true);
            hook.setProcessedAt(LocalDateTime.now());
            webhookRepository.save(hook);
            return "IGNORED";
        }

        String orderId = entityNode.path("order_id").asText(null);
        String gatewayPaymentId = entityNode.path("id").asText(null);

        var optTx = transactionRepository.findByRazorpayOrderId(orderId);
        if (optTx.isEmpty()) {
            hook.setProcessed(true);
            hook.setProcessedAt(LocalDateTime.now());
            webhookRepository.save(hook);
            return "UNKNOWN_ORDER";
        }
        PaymentTransaction tx = optTx.get();
        if (tx.getStatus() != TransactionStatus.CAPTURED) {
            capture(tx, tx.getPayment(), gatewayPaymentId,
                    hmacSha256Hex((orderId != null ? orderId : "") + "|" + (gatewayPaymentId != null ? gatewayPaymentId : ""), verifySecret()),
                    null);
        }
        hook.setProcessed(true);
        hook.setProcessedAt(LocalDateTime.now());
        webhookRepository.save(hook);
        return "PROCESSED";
    }

    // ---------------------------------------------------------------- offline (admin)

    @Transactional
    public PaymentResponse recordOffline(String adminEmail, OfflinePaymentRequest request) {
        User recorder = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot record payment on a cancelled invoice");
        }
        BigDecimal balance = nvl(invoice.getBalanceAmount());
        if (request.amount().compareTo(balance) > 0) {
            throw new BadRequestException("Amount exceeds outstanding balance of " + balance.toPlainString());
        }

        Payment payment = new Payment();
        payment.setClient(invoice.getClient());
        payment.setInvoice(invoice);
        payment.setProformaInvoice(invoice.getProformaInvoice());
        payment.setPaymentType(request.amount().compareTo(balance) == 0 ? PaymentType.FULL : PaymentType.PARTIAL);
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.PAID);
        payment.setAmount(request.amount());
        payment.setCurrency(invoice.getCurrency());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionReference(request.transactionReference());
        payment.setNotes(request.notes());
        payment.setRecordedBy(recorder);
        paymentRepository.save(payment);

        applyToInvoice(payment);
        receiptService.issueForPayment(payment);
        return toResponse(payment);
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listMine(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findByUserId(user.getId()).orElse(null);
        if (client == null) {
            return Page.empty(pageable);
        }
        return paymentRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getForManager(Long id) {
        return paymentRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    // ---------------------------------------------------------------- internals

    /** Single source of truth for settling money against an invoice. */
    private void applyToInvoice(Payment payment) {
        Invoice invoice = payment.getInvoice();
        BigDecimal newPaid = nvl(invoice.getPaidAmount()).add(nvl(payment.getAmount()));
        BigDecimal grandTotal = nvl(invoice.getGrandTotal());
        BigDecimal newBalance = grandTotal.subtract(newPaid).max(BigDecimal.ZERO);

        invoice.setPaidAmount(newPaid);
        invoice.setBalanceAmount(newBalance);
        invoice.setStatus(newBalance.signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);

        ProformaInvoice pi = invoice.getProformaInvoice();
        if (pi != null && (pi.getStatus() == ProformaInvoiceStatus.SENT
                || pi.getStatus() == ProformaInvoiceStatus.PAYMENT_PENDING)) {
            pi.setStatus(ProformaInvoiceStatus.ADVANCE_PAID);
        }

        notificationService.notify(
                invoice.getClient().getUser(),
                NotificationType.PAYMENT,
                "Payment received for " + invoice.getInvoiceNo(),
                "We have received " + payment.getCurrency() + " " + nvl(payment.getAmount()).toPlainString()
                        + ". Outstanding balance: " + invoice.getCurrency() + " " + newBalance.toPlainString() + ".",
                "/client/invoices",
                "INVOICE",
                invoice.getId());
    }

    private void capture(PaymentTransaction tx, Payment payment, String gatewayPaymentId, String signature, String rawResponse) {
        tx.setStatus(TransactionStatus.CAPTURED);
        tx.setRazorpayPaymentId(gatewayPaymentId);
        tx.setRazorpaySignature(signature);
        tx.setCapturedAt(LocalDateTime.now());
        if (rawResponse != null) {
            tx.setRawResponse(rawResponse.length() > 4000 ? rawResponse.substring(0, 4000) : rawResponse);
        }
        transactionRepository.save(tx);

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionReference(gatewayPaymentId);
        paymentRepository.save(payment);

        applyToInvoice(payment);
        receiptService.issueForPayment(payment);
        log.info("Payment captured: invoice={}, gateway payment={}",
                payment.getInvoice() != null ? payment.getInvoice().getInvoiceNo() : "-", gatewayPaymentId);
    }

    private PaymentResponse toResponse(Payment p) {
        Invoice inv = p.getInvoice();
        ProformaInvoice pi = p.getProformaInvoice();
        return new PaymentResponse(
                p.getId(),
                inv != null ? inv.getId() : null,
                inv != null ? inv.getInvoiceNo() : null,
                pi != null ? pi.getId() : null,
                pi != null ? pi.getPiNo() : null,
                p.getPaymentType(),
                p.getMethod(),
                p.getStatus(),
                p.getAmount(),
                p.getCurrency(),
                p.getPaidAt(),
                p.getTransactionReference(),
                p.getRazorpayOrderId(),
                p.getNotes(),
                p.getCreatedAt());
    }

    static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    static long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    static String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }

    static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                b.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }
}
