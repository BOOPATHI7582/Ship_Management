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
 * Public contact-form submissions.
 */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
public class ContactMessage extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "company", length = 200)
    private String company;

    @Column(name = "subject", length = 255)
    private String subject;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "handled", nullable = false)
    private boolean handled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_id")
    private User handledBy;

    @Column(name = "handled_at")
    private java.time.LocalDateTime handledAt;
}
