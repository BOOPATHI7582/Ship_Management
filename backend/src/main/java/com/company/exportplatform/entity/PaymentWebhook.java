package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Raw webhook event log. event_id UNIQUE makes processing idempotent:
 * duplicate events are detected and skipped.
 */
@Entity
@Table(name = "payment_webhooks")
@Getter
@Setter
public class PaymentWebhook extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "payload")
    private String payload;

    @Column(name = "signature", length = 255)
    private String signature;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
