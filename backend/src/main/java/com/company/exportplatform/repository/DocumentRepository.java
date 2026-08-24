package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Document;
import com.company.exportplatform.entity.enums.DocumentOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(DocumentOwnerType ownerType, Long ownerId);
}
