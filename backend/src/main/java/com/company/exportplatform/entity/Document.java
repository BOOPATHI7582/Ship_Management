package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.DocumentCategory;
import com.company.exportplatform.entity.enums.DocumentOwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cloudinary file metadata only (URLs + public ids) - MySQL never stores blobs.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 40)
    private DocumentOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private DocumentCategory category;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "secure_url", nullable = false, length = 500)
    private String secureUrl;

    @Column(name = "file_format", length = 20)
    private String fileFormat;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;
}
