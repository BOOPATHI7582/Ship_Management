package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.ContactMessageResponse;
import com.company.exportplatform.entity.ContactMessage;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ContactMessageRepository;
import com.company.exportplatform.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff inbox over public contact-form submissions plus the
 * client-facing "my messages" view matched by account email.
 */
@Service
@RequiredArgsConstructor
public class ContactInboxService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<ContactMessageResponse> list(String status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<ContactMessage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if ("NEW".equalsIgnoreCase(status)) {
                predicates.add(cb.isFalse(root.get("handled")));
            } else if ("HANDLED".equalsIgnoreCase(status)) {
                predicates.add(cb.isTrue(root.get("handled")));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                Predicate name = cb.like(cb.lower(root.get("fullName")), like);
                Predicate email = cb.like(cb.lower(root.get("email")), like);
                Predicate subject = cb.like(cb.lower(root.get("subject")), like);
                predicates.add(cb.or(name, email, subject));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return contactMessageRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return contactMessageRepository.countByHandledFalse();
    }

    @Transactional
    public ContactMessageResponse markHandled(Long id, String staffEmail) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        message.setHandled(true);
        message.setHandledAt(java.time.LocalDateTime.now());
        userRepository.findByEmail(staffEmail).ifPresent(message::setHandledBy);
        auditService.record(staffEmail, "CONTACT_HANDLED", "CONTACT_MESSAGE", id,
                null, message.getSubject());
        return toResponse(message);
    }

    @Transactional
    public ContactMessageResponse reopen(Long id, String staffEmail) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        message.setHandled(false);
        message.setHandledAt(null);
        message.setHandledBy(null);
        auditService.record(staffEmail, "CONTACT_REOPENED", "CONTACT_MESSAGE", id,
                null, message.getSubject());
        return toResponse(message);
    }

    /** Messages the authenticated client submitted themselves (matched by email). */
    @Transactional(readOnly = true)
    public Page<ContactMessageResponse> myMessages(String email, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return contactMessageRepository
                .findByEmailIgnoreCaseOrderByCreatedAtDesc(email, pageable)
                .map(this::toResponse);
    }

    private ContactMessageResponse toResponse(ContactMessage m) {
        User handledBy = m.getHandledBy();
        return new ContactMessageResponse(
                m.getId(),
                m.getFullName(),
                m.getEmail(),
                m.getPhone(),
                m.getCompany(),
                m.getSubject(),
                m.getMessage(),
                m.isHandled(),
                handledBy != null ? handledBy.getFullName() : null,
                m.getHandledAt(),
                m.getCreatedAt());
    }
}
