package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Dynamic cargo taxonomy managed by admin - never hard-coded.
 */
@Entity
@Table(name = "cargo_categories")
@Getter
@Setter
public class CargoCategory extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
