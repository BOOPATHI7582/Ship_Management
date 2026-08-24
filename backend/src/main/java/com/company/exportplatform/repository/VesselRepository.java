package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Vessel;
import com.company.exportplatform.entity.enums.VesselStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VesselRepository extends JpaRepository<Vessel, Long>, JpaSpecificationExecutor<Vessel> {

    Optional<Vessel> findByImoNumber(String imoNumber);

    boolean existsByImoNumber(String imoNumber);

    List<Vessel> findByStatus(VesselStatus status);

    Page<Vessel> findByStatus(VesselStatus status, Pageable pageable);

    long countByStatusIn(Collection<VesselStatus> statuses);
}
