package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "receipts")
@Getter
@Setter
public class Receipt extends BaseEntity {

    @Column(name = "receipt_no", nullable = false, unique = true, length = 30)
    private String receiptNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "issued_on", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 30)
    private PaymentMethod method;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    /** Balance remaining on the linked invoice after this payment. */
    @Column(name = "remaining_balance", nullable = false, precision = 18, scale = 4)
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "pdf_public_id", length = 255)
    private String pdfPublicId;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;
}
