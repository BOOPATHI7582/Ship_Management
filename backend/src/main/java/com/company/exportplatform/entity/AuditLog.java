package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable financial/operational audit trail. Old/new values are JSON
 * snapshots; rows are never updated or deleted.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    /** Email snapshot in case the user record is ever removed. */
    @Column(name = "actor_email", length = 150)
    private String actorEmail;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "old_value")
    private String oldValue;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "new_value")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
