package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Backend-controlled sequential document numbers, e.g. QUO-2026-000001.
 * Row (docType, docYear) is pessimistic-locked when incrementing so
 * duplicate document numbers are impossible. Not audited (no timestamps).
 */
@Entity
@Table(name = "document_sequences")
@Getter
@Setter
@IdClass(DocumentSequenceId.class)
public class DocumentSequence implements Serializable {

    @Id
    @Column(name = "doc_type", nullable = false, length = 30)
    private String docType;

    @Id
    @Column(name = "doc_year", nullable = false)
    private Integer docYear;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber = 0L;
}
