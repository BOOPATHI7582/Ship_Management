package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.ShipmentStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Operational shipment with 11-state forward-only lifecycle.
 * tracking_token powers public tracking without exposing internal IDs.
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
public class Shipment extends BaseEntity {

    @Column(name = "shipment_ref", nullable = false, unique = true, length = 30)
    private String shipmentRef;

    @Column(name = "tracking_token", nullable = false, unique = true, length = 64)
    private String trackingToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id")
    private Vessel vessel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_invoice_id")
    private ProformaInvoice proformaInvoice;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "origin_country", length = 80)
    private String originCountry;

    @Column(name = "destination_country", length = 80)
    private String destinationCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loading_port_id")
    private Port loadingPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id")
    private Port destinationPort;

    @Column(name = "loading_date")
    private LocalDate loadingDate;

    @Column(name = "estimated_arrival")
    private LocalDate estimatedArrival;

    @Column(name = "actual_arrival")
    private LocalDate actualArrival;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "final_price", precision = 18, scale = 4)
    private BigDecimal finalPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ShipmentStatus status = ShipmentStatus.BOOKING_CONFIRMED;

    @Column(name = "current_location", length = 200)
    private String currentLocation;

    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @Column(name = "last_tracked_at")
    private LocalDateTime lastTrackedAt;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
