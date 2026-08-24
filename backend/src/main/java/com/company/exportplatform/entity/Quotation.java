package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.QuotationStatus;
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

/**
 * Commercial quotation. All financial totals are computed backend-side with
 * BigDecimal and snapshotted at creation; address fields are point-in-time
 * copies so historical documents never change.
 */
@Entity
@Table(name = "quotations")
@Getter
@Setter
public class Quotation extends BaseEntity {

    @Column(name = "quote_no", nullable = false, unique = true, length = 30)
    private String quoteNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    // --- billing snapshot ---
    @Column(name = "billing_address_line1", length = 255)
    private String billingAddressLine1;
    @Column(name = "billing_address_line2", length = 255)
    private String billingAddressLine2;
    @Column(name = "billing_city", length = 100)
    private String billingCity;
    @Column(name = "billing_state", length = 100)
    private String billingState;
    @Column(name = "billing_postal_code", length = 20)
    private String billingPostalCode;
    @Column(name = "billing_country", length = 80)
    private String billingCountry;

    // --- shipping snapshot ---
    @Column(name = "shipping_address_line1", length = 255)
    private String shippingAddressLine1;
    @Column(name = "shipping_address_line2", length = 255)
    private String shippingAddressLine2;
    @Column(name = "shipping_city", length = 100)
    private String shippingCity;
    @Column(name = "shipping_state", length = 100)
    private String shippingState;
    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;
    @Column(name = "shipping_country", length = 80)
    private String shippingCountry;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "incoterms", length = 20)
    private String incoterms;

    @Column(name = "payment_terms", length = 1000)
    private String paymentTerms;

    @Column(name = "delivery_terms", length = 1000)
    private String deliveryTerms;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "notes")
    private String notes;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "terms_conditions")
    private String termsConditions;

    // --- financials: BigDecimal only, recalculated server-side ---
    @Column(name = "subtotal", nullable = false, precision = 18, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "discount", nullable = false, precision = 18, scale = 4)
    private BigDecimal discount = BigDecimal.ZERO;
    @Column(name = "freight_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal freightCharges = BigDecimal.ZERO;
    @Column(name = "loading_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal loadingCharges = BigDecimal.ZERO;
    @Column(name = "documentation_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal documentationCharges = BigDecimal.ZERO;
    @Column(name = "insurance_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal insuranceCharges = BigDecimal.ZERO;
    @Column(name = "other_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal otherCharges = BigDecimal.ZERO;
    @Column(name = "taxable_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal taxableAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_treatment", length = 40)
    private TaxTreatment taxTreatment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "grand_total", nullable = false, precision = 18, scale = 4)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuotationStatus status = QuotationStatus.DRAFT;

    /** Public secure token for /quotation/:secureToken - hides internal IDs. */
    @Column(name = "secure_token", nullable = false, unique = true, length = 64)
    private String secureToken;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("itemOrder ASC")
    private List<QuotationItem> items = new ArrayList<>();
}
