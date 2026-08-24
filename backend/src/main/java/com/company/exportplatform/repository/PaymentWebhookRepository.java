package com.company.exportplatform.repository;

import com.company.exportplatform.entity.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    Optional<PaymentWebhook> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
