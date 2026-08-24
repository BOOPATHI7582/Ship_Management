package com.company.exportplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "proforma_invoice_items")
@Getter
@Setter
public class ProformaInvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proforma_invoice_id", nullable = false)
    private ProformaInvoice proformaInvoice;

    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "rate_per_unit", nullable = false, precision = 18, scale = 4)
    private BigDecimal ratePerUnit;

    @Column(name = "line_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal lineAmount;
}
