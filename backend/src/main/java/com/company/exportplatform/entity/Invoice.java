package com.company.exportplatform.entity;

import com.company.exportplatform.entity.enums.InvoiceStatus;
import com.company.exportplatform.entity.enums.InvoiceType;
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
 * Tax invoice / final bill. CGST/SGST/IGST amounts are stored per document
 * (rate snapshot at issue time); historical documents are never recalculated.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice extends BaseEntity {

    @Column(name = "invoice_no", nullable = false, unique = true, length = 30)
    private String invoiceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 30)
    private InvoiceType invoiceType = InvoiceType.TAX_INVOICE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_invoice_id")
    private ProformaInvoice proformaInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

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

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "pan", length = 20)
    private String pan;

    @Column(name = "place_of_supply", length = 120)
    private String placeOfSupply;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    /** FX rate snapshot for export invoices (foreign currency -> INR). */
    @Column(name = "exchange_rate", precision = 14, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "incoterms", length = 20)
    private String incoterms;

    @Column(name = "port_of_loading", length = 150)
    private String portOfLoading;

    @Column(name = "port_of_discharge", length = 150)
    private String portOfDischarge;

    @Column(name = "export_reference", length = 100)
    private String exportReference;

    // --- financials (BigDecimal only, computed server-side) ---
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
    @Column(name = "additional_charges", nullable = false, precision = 18, scale = 4)
    private BigDecimal additionalCharges = BigDecimal.ZERO;
    @Column(name = "taxable_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal taxableAmount = BigDecimal.ZERO;
    @Column(name = "cgst_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal cgstAmount = BigDecimal.ZERO;
    @Column(name = "sgst_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal sgstAmount = BigDecimal.ZERO;
    @Column(name = "igst_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal igstAmount = BigDecimal.ZERO;
    @Column(name = "other_tax_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal otherTaxAmount = BigDecimal.ZERO;
    @Column(name = "total_tax_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalTaxAmount = BigDecimal.ZERO;
    @Column(name = "grand_total", nullable = false, precision = 18, scale = 4)
    private BigDecimal grandTotal = BigDecimal.ZERO;
    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    @Column(name = "balance_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_treatment", length = 40)
    private TaxTreatment taxTreatment;

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

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    @Column(name = "terms_conditions")
    private String termsConditions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "pdf_public_id", length = 255)
    private String pdfPublicId;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("itemOrder ASC")
    private List<InvoiceItem> items = new ArrayList<>();
}
