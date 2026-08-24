package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.CargoStatus;
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
@Table(name = "cargo")
@Getter
@Setter
public class Cargo extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CargoCategory category;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "description")
    private String description;

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

    @Column(name = "indicative_price", precision = 18, scale = 4)
    private BigDecimal indicativePrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private CargoStatus status = CargoStatus.AVAILABLE;
}
