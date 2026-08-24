package com.company.exportplatform.repository;

import com.company.exportplatform.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceIdOrderByItemOrderAsc(Long invoiceId);
}
