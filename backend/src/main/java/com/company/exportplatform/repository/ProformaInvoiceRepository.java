package com.company.exportplatform.repository;

import com.company.exportplatform.entity.ProformaInvoice;
import com.company.exportplatform.entity.enums.ProformaInvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProformaInvoiceRepository
        extends JpaRepository<ProformaInvoice, Long>, JpaSpecificationExecutor<ProformaInvoice> {

    Optional<ProformaInvoice> findByPiNo(String piNo);

    Page<ProformaInvoice> findByClientId(Long clientId, Pageable pageable);

    Page<ProformaInvoice> findByStatus(ProformaInvoiceStatus status, Pageable pageable);

    long countByClientId(Long clientId);
}
