package com.company.exportplatform.repository;

import com.company.exportplatform.entity.VesselImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VesselImageRepository extends JpaRepository<VesselImage, Long> {

    List<VesselImage> findByVesselIdOrderBySortOrderAsc(Long vesselId);
}
