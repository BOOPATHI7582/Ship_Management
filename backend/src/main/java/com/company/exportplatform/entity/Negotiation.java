package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.NegotiationThreadStatus;
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
import java.time.LocalDateTime;

/**
 * One negotiation thread per enquiry; messages are append-only history.
 */
@Entity
@Table(name = "negotiations")
@Getter
@Setter
public class Negotiation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opened_by_id", nullable = false)
    private User openedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private NegotiationThreadStatus status = NegotiationThreadStatus.OPEN;

    @Column(name = "agreed_price", precision = 18, scale = 4)
    private BigDecimal agreedPrice;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

}
