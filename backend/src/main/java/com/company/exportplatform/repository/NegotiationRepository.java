package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Negotiation;
import com.company.exportplatform.entity.enums.NegotiationThreadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    List<Negotiation> findByEnquiryIdOrderByCreatedAtDesc(Long enquiryId);

    Optional<Negotiation> findFirstByEnquiryIdAndStatusOrderByCreatedAtDesc(Long enquiryId, NegotiationThreadStatus status);

    long countByStatus(NegotiationThreadStatus status);

    long countByEnquiry_ClientIdAndStatus(Long clientId, NegotiationThreadStatus status);
}
