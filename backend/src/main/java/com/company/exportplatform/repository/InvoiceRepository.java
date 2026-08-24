package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Invoice;
import com.company.exportplatform.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    Page<Invoice> findByClientId(Long clientId, Pageable pageable);

    List<Invoice> findByProformaInvoiceIdAndStatusNot(Long proformaInvoiceId, InvoiceStatus status);

    long countByClientId(Long clientId);

    long countByStatus(InvoiceStatus status);

    @Query("select coalesce(sum(i.grandTotal - i.paidAmount), 0) from Invoice i "
            + "where i.client.id = :clientId and i.status in :statuses")
    java.math.BigDecimal sumOutstandingByClientId(@Param("clientId") Long clientId,
                                                  @Param("statuses") Collection<InvoiceStatus> statuses);

    @Query("select coalesce(sum(i.grandTotal), 0.0) from Invoice i")
    BigDecimal sumGrandTotal();

    @Query("select coalesce(sum(i.paidAmount), 0.0) from Invoice i")
    BigDecimal sumPaidAmount();

    @Query("select coalesce(sum(i.balanceAmount), 0.0) from Invoice i where i.status <> :excludedStatus")
    BigDecimal sumBalanceExcludingStatus(@Param("excludedStatus") InvoiceStatus excludedStatus);

    @Query("select function('date_format', i.issueDate, '%Y-%m'), coalesce(sum(i.grandTotal), 0) from Invoice i "
            + "where i.status <> :excludedStatus "
            + "group by function('date_format', i.issueDate, '%Y-%m') order by 1 asc")
    List<Object[]> sumGrandTotalByMonth(@Param("excludedStatus") InvoiceStatus excludedStatus);

    @Query("select i.client.id, coalesce(i.client.user.companyName, i.client.user.fullName), coalesce(sum(i.grandTotal - i.paidAmount), 0) from Invoice i "
            + "where i.status in :statuses "
            + "group by i.client.id, i.client.user.companyName, i.client.user.fullName order by 3 desc")
    List<Object[]> sumOutstandingByClient(@Param("statuses") Collection<InvoiceStatus> statuses, Pageable pageable);
}