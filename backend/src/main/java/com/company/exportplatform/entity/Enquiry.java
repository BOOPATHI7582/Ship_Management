package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.EnquiryStatus;
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

@Entity
@Table(name = "enquiries")
@Getter
@Setter
public class Enquiry extends BaseEntity {

    @Column(name = "reference_no", nullable = false, unique = true, length = 30)
    private String referenceNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "cargo_type", nullable = false, length = 200)
    private String cargoType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_category_id")
    private CargoCategory cargoCategory;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "cargo_description")
    private String cargoDescription;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "origin_country", length = 80)
    private String originCountry;

    @Column(name = "origin_location", length = 200)
    private String originLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loading_port_id")
    private Port loadingPort;

    @Column(name = "destination_country", length = 80)
    private String destinationCountry;

    @Column(name = "destination_location", length = 200)
    private String destinationLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id")
    private Port destinationPort;

    @Column(name = "required_loading_date")
    private LocalDate requiredLoadingDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "estimated_budget", precision = 18, scale = 4)
    private BigDecimal estimatedBudget;

    @Column(name = "target_price_per_unit", precision = 18, scale = 4)
    private BigDecimal targetPricePerUnit;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private EnquiryStatus status = EnquiryStatus.NEW;
}
