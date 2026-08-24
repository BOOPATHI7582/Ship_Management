package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.AuditLogResponse;
import com.company.exportplatform.entity.AuditLog;
import com.company.exportplatform.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/manager/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManagerAuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ApiResponse<Page<AuditLogResponse>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (StringUtils.hasText(entityType)) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("actorEmail")), like),
                        cb.like(cb.lower(root.get("newValue")), like),
                        cb.like(cb.lower(root.get("oldValue")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        return ApiResponse.ok("Audit log", page.map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActorEmail(), log.getAction(),
                log.getEntityType(), log.getEntityId(), log.getOldValue(), log.getNewValue(),
                log.getIpAddress(), log.getCreatedAt());
    }
}
