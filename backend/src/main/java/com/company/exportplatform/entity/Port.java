package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Reusable port master data (UN/LOCODE-style code) referenced by cargo,
 * enquiries, quotations and shipments.
 */
@Entity
@Table(name = "ports")
@Getter
@Setter
public class Port extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "country", nullable = false, length = 80)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
