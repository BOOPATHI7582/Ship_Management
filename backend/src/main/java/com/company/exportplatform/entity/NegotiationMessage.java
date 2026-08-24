package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.NegotiationMessageStatus;
import com.company.exportplatform.entity.enums.SenderType;
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

/**
 * Immutable negotiation offer/message - history is never overwritten.
 */
@Entity
@Table(name = "negotiation_messages")
@Getter
@Setter
public class NegotiationMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Column(name = "offer_price", precision = 18, scale = 4)
    private BigDecimal offerPrice;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NegotiationMessageStatus status = NegotiationMessageStatus.PROPOSED;
}
