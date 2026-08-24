package com.company.exportplatform.repository;

import com.company.exportplatform.entity.DocumentSequence;
import com.company.exportplatform.entity.DocumentSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, DocumentSequenceId> {

    /**
     * Pessimistic row lock so concurrent document creation can never
     * produce duplicate document numbers.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DocumentSequence s where s.docType = :docType and s.docYear = :docYear")
    Optional<DocumentSequence> findForUpdate(@Param("docType") String docType, @Param("docYear") Integer docYear);
}
