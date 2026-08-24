package com.company.exportplatform.repository;

import com.company.exportplatform.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContactMessageRepository
        extends JpaRepository<ContactMessage, Long>, JpaSpecificationExecutor<ContactMessage> {

    Page<ContactMessage> findAll(Pageable pageable);

    List<ContactMessage> findByHandledFalseOrderByCreatedAtDesc();

    long countByHandledFalse();

    Page<ContactMessage> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);
}
