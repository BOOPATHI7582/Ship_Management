package com.company.exportplatform.repository;

import com.company.exportplatform.entity.NegotiationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationMessageRepository extends JpaRepository<NegotiationMessage, Long> {

    List<NegotiationMessage> findByNegotiationIdOrderByCreatedAtAsc(Long negotiationId);
}
