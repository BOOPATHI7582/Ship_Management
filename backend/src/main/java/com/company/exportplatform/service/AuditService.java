package com.company.exportplatform.service;

import com.company.exportplatform.entity.AuditLog;
import com.company.exportplatform.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes immutable audit entries. Failures never break the business action:
 * they are logged and swallowed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actorEmail, String action, String entityType, Long entityId,
                       Object oldValue, Object newValue) {
        try {
            AuditLog entry = new AuditLog();
            if (actorEmail == null || actorEmail.isBlank()) {
                actorEmail = currentUsername();
            }
            entry.setActorEmail(actorEmail);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setOldValue(toJson(oldValue));
            entry.setNewValue(toJson(newValue));
            entry.setIpAddress(currentIp());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to write audit log for {} {}: {}", action, entityType, ex.getMessage());
        }
    }

    public void record(String action, String entityType, Long entityId, Object oldValue, Object newValue) {
        record(null, action, entityType, entityId, oldValue, newValue);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return attrs.getRequest().getRemoteAddr();
        }
        return null;
    }
}
