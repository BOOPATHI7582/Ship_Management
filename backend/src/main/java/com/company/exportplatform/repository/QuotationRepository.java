package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface QuotationRepository extends JpaRepository<Quotation, Long>, JpaSpecificationExecutor<Quotation> {

    Optional<Quotation> findByQuoteNo(String quoteNo);

    Optional<Quotation> findBySecureToken(String secureToken);

    Page<Quotation> findByClientId(Long clientId, Pageable pageable);

    Page<Quotation> findByStatus(QuotationStatus status, Pageable pageable);

    List<Quotation> findByEnquiryId(Long enquiryId);

    long countByEnquiryId(Long enquiryId);

    Optional<Quotation> findByIdAndClient_User_EmailIgnoreCase(Long id, String email);

    long countByClientId(Long clientId);

    long countByClientIdAndStatus(Long clientId, QuotationStatus status);
}
