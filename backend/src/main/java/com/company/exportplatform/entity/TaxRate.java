package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.TaxType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Configurable tax row - admin managed; rates are never hard-coded.
 */
@Entity
@Table(name = "tax_rates")
@Getter
@Setter
public class TaxRate extends BaseEntity {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 30)
    private TaxType taxType;

    @Column(name = "rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal rate;

    @Column(name = "country", nullable = false, length = 80)
    private String country;

    @Column(name = "jurisdiction", length = 120)
    private String jurisdiction;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "description", length = 500)
    private String description;
}
