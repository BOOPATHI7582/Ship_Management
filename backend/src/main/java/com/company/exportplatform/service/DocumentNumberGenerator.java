package com.company.exportplatform.service;

import com.company.exportplatform.entity.DocumentSequence;
import com.company.exportplatform.entity.DocumentSequenceId;
import com.company.exportplatform.repository.DocumentSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Duplicate-proof document numbers: PREFIX-YYYY-NNNNNN.
 * The (doc_type, doc_year) row is pessimistic-locked inside the caller's
 * transaction so concurrent requests serialize on the counter row.
 */
@Service
@RequiredArgsConstructor
public class DocumentNumberGenerator {

    private final DocumentSequenceRepository sequenceRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next(String docType, String prefix) {
        int year = java.time.Year.now().getValue();
        DocumentSequenceId id = new DocumentSequenceId(docType, year);
        DocumentSequence sequence = sequenceRepository.findForUpdate(docType, year)
                .orElseGet(() -> {
                    DocumentSequence fresh = new DocumentSequence();
                    fresh.setDocType(id.getDocType());
                    fresh.setDocYear(id.getDocYear());
                    fresh.setLastNumber(0L);
                    return fresh;
                });
        long next = sequence.getLastNumber() + 1;
        sequence.setLastNumber(next);
        sequenceRepository.save(sequence);
        return "%s-%d-%06d".formatted(prefix, year, next);
    }
}
