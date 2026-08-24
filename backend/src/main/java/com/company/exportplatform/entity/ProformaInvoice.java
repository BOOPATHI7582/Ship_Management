package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.ProformaInvoiceStatus;
import com.company.exportplatform.entity.enums.TaxTreatment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proforma_invoices")
@Getter
@Setter
public class ProformaInvoice extends BaseEntity {

    @Column(name = "pi_no", nullable = false, unique = true, length = 30)
    private String piNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "discount", nullable = false, precision = 18, scale = 4)
    private BigDecimal discount = BigDecimal.ZERO;
    @Column(name = "taxable_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal taxableAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_treatment", length = 40)
    private TaxTreatment taxTreatment;
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "grand_total", nullable = false, precision = 18, scale = 4)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "payment_terms", length = 1000)
    private String paymentTerms;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "bank_details")
    private String bankDetails;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProformaInvoiceStatus status = ProformaInvoiceStatus.DRAFT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "pdf_public_id", length = 255)
    private String pdfPublicId;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "proformaInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("itemOrder ASC")
    private List<ProformaInvoiceItem> items = new ArrayList<>();
}
