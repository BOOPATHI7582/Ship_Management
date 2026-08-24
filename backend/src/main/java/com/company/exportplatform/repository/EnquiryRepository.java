package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long>, JpaSpecificationExecutor<Enquiry> {

    Optional<Enquiry> findByReferenceNo(String referenceNo);

    Page<Enquiry> findByClientId(Long clientId, Pageable pageable);

    Page<Enquiry> findByStatus(EnquiryStatus status, Pageable pageable);

    long countByClientId(Long clientId);

    long countByStatus(EnquiryStatus status);

    long countByClientIdAndStatusIn(Long clientId, Collection<EnquiryStatus> statuses);

    @org.springframework.data.jpa.repository.Query("select e.status, count(e) from Enquiry e group by e.status")
    java.util.List<Object[]> countGroupedByStatus();
}
