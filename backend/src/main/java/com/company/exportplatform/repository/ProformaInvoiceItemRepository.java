package com.company.exportplatform.repository;

import com.company.exportplatform.entity.ProformaInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProformaInvoiceItemRepository extends JpaRepository<ProformaInvoiceItem, Long> {

    List<ProformaInvoiceItem> findByProformaInvoiceIdOrderByItemOrderAsc(Long proformaInvoiceId);
}
