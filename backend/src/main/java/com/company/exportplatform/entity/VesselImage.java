package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cloudinary metadata only - no binary data in MySQL.
 */
@Entity
@Table(name = "vessel_images")
@Getter
@Setter
public class VesselImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "secure_url", nullable = false, length = 500)
    private String secureUrl;

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
