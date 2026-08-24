package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByShipmentRef(String shipmentRef);

    Optional<Shipment> findByTrackingToken(String trackingToken);

    Page<Shipment> findByClientId(Long clientId, Pageable pageable);

    List<Shipment> findByStatus(ShipmentStatus status);

    long countByStatus(ShipmentStatus status);

    long countByClientId(Long clientId);

    long countByClientIdAndStatusIn(Long clientId, Collection<ShipmentStatus> statuses);
}
