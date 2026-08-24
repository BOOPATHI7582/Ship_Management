package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Port;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PortRepository extends JpaRepository<Port, Long>, JpaSpecificationExecutor<Port> {

    Optional<Port> findByCode(String code);

    boolean existsByCode(String code);

    List<Port> findByActiveTrue();

    long countByActiveTrue();

    Page<Port> findAll(Pageable pageable);
}
