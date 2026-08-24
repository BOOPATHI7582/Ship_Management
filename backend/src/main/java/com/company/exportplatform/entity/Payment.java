package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.PaymentMethod;
import com.company.exportplatform.entity.enums.PaymentStatus;
import com.company.exportplatform.entity.enums.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment intent/record. Gateway rows live in payment_transactions;
 * offline payments carry transaction_reference + recorded_by (audited).
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_invoice_id")
    private ProformaInvoice proformaInvoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType = PaymentType.ADVANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method = PaymentMethod.RAZORPAY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    /** Convenience copy of the active Razorpay order id (canonical row is in payment_transactions). */
    @Column(name = "razorpay_order_id", length = 64)
    private String razorpayOrderId;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private User recordedBy;
}
