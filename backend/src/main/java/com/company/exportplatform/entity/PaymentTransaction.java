package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.TransactionStatus;
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

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "gateway", nullable = false, length = 30)
    private String gateway = "RAZORPAY";

    @Column(name = "razorpay_order_id", unique = true, length = 64)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true, length = 64)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransactionStatus status = TransactionStatus.CREATED;

    @Column(name = "error_description", length = 500)
    private String errorDescription;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "raw_response")
    private String rawResponse;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
}
