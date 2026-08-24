package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Post-delivery client review; only COMPLETED shipments may be reviewed.
 * Public visibility requires approved = true.
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title", length = 150)
    private String title;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by_id")
    private User moderatedBy;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;
}
