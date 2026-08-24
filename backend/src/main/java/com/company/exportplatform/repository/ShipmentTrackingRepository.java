package com.company.exportplatform.repository;

import com.company.exportplatform.entity.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {

    List<ShipmentTracking> findByShipmentIdOrderByOccurredAtDesc(Long shipmentId);

    Optional<ShipmentTracking> findFirstByShipmentIdOrderByOccurredAtDesc(Long shipmentId);
}
