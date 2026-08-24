package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.VesselStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.List;

@Entity
@Table(name = "vessels")
@Getter
@Setter
public class Vessel extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "imo_number", nullable = false, unique = true, length = 20)
    private String imoNumber;

    @Column(name = "vessel_type", length = 80)
    private String vesselType;

    @Column(name = "capacity", precision = 18, scale = 4)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 30)
    private String capacityUnit;

    @Column(name = "flag", length = 80)
    private String flag;

    @Column(name = "current_location", length = 200)
    private String currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private VesselStatus status = VesselStatus.AVAILABLE;

    /**
     * Sensitive management info - only exposed to authorized roles via DTOs.
     */
    @Column(name = "management_company", length = 200)
    private String managementCompany;

    @Column(name = "management_contact", length = 120)
    private String managementContact;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "vessel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<VesselImage> images = new ArrayList<>();
}
