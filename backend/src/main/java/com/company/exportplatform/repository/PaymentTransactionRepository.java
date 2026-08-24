package com.company.exportplatform.repository;

import com.company.exportplatform.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);

    Optional<PaymentTransaction> findByRazorpayPaymentId(String razorpayPaymentId);
}
