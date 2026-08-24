package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Payment;
import com.company.exportplatform.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Page<Payment> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    long countByStatus(PaymentStatus status);

    long countByClientIdAndStatusIn(Long clientId, java.util.Collection<PaymentStatus> statuses);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    List<Payment> findByInvoiceIdAndStatusOrderByPaidAtDesc(Long invoiceId, PaymentStatus status);

    @Query("select function('date_format', p.paidAt, '%Y-%m'), coalesce(sum(p.amount), 0) from Payment p "
            + "where p.status = :status and p.paidAt is not null "
            + "group by function('date_format', p.paidAt, '%Y-%m') order by 1 asc")
    List<Object[]> sumAmountByMonth(@Param("status") PaymentStatus status);

    @Query("select p.client.id, coalesce(p.client.user.companyName, p.client.user.fullName), coalesce(sum(p.amount), 0) from Payment p "
            + "where p.status = :status "
            + "group by p.client.id, p.client.user.companyName, p.client.user.fullName order by 3 desc")
    List<Object[]> sumRevenueByClient(@Param("status") PaymentStatus status, Pageable pageable);
}
