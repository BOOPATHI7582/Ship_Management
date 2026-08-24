package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Cargo;
import com.company.exportplatform.entity.enums.CargoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long>, JpaSpecificationExecutor<Cargo> {

    Page<Cargo> findByStatus(CargoStatus status, Pageable pageable);

    List<Cargo> findByStatus(CargoStatus status);

    long countByStatus(CargoStatus status);
}
