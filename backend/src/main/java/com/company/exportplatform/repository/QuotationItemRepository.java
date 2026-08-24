package com.company.exportplatform.repository;

import com.company.exportplatform.entity.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {

    List<QuotationItem> findByQuotationIdOrderByItemOrderAsc(Long quotationId);
}
