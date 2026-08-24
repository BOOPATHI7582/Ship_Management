package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptNo(String receiptNo);

    Optional<Receipt> findByPaymentId(Long paymentId);

    Page<Receipt> findByClientId(Long clientId, Pageable pageable);
}
