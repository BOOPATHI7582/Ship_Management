-- =============================================================================
-- ExportPlatform - Initial Schema (Phase 2)
-- Global Export, Cargo, Vessel, Shipment, Quotation, Tax Invoice, Payment,
-- Billing & Tracking Management Platform
--
-- Conventions:
--   * InnoDB / utf8mb4, BIGINT auto-increment surrogate keys
--   * DECIMAL(18,4) money & quantities  |  DECIMAL(9,4) tax rates
--   * DATE = pure dates                 |  DATETIME(6) timestamps (UTC app-side)
--   * TINYINT(1) booleans               |  status/type columns are VARCHAR enums
--   * ON DELETE RESTRICT everywhere (financial history is never orphaned/cascaded away)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- IDENTITY & ROLES
-- -----------------------------------------------------------------------------

CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(150)  NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,
    full_name     VARCHAR(150)  NOT NULL,
    company_name  VARCHAR(200)  NULL,
    phone         VARCHAR(30)   NULL,
    country       VARCHAR(80)   NULL,
    active        TINYINT(1)    NOT NULL DEFAULT 1,
    role_id       BIGINT        NOT NULL,
    last_login_at DATETIME(6)   NULL,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role (role_id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE clients (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    gstin          VARCHAR(20)  NULL,
    address_line1  VARCHAR(255) NULL,
    address_line2  VARCHAR(255) NULL,
    city           VARCHAR(100) NULL,
    state          VARCHAR(100) NULL,
    postal_code    VARCHAR(20)  NULL,
    country        VARCHAR(80)  NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_clients_user (user_id),
    CONSTRAINT fk_clients_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE password_resets (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    user_id    BIGINT        NOT NULL,
    token_hash VARCHAR(128)  NOT NULL,
    expires_at DATETIME(6)   NOT NULL,
    used_at    DATETIME(6)   NULL,
    created_at DATETIME(6)   NULL,
    updated_at DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_resets_token (token_hash),
    KEY idx_password_resets_user (user_id),
    CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- MASTER DATA: PORTS, CARGO CATEGORIES, CARGO, VESSELS
-- -----------------------------------------------------------------------------

CREATE TABLE ports (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(150)  NOT NULL,
    country    VARCHAR(80)   NOT NULL,
    city       VARCHAR(100)  NULL,
    code       VARCHAR(10)   NOT NULL,
    latitude   DECIMAL(10,7) NULL,
    longitude  DECIMAL(10,7) NULL,
    active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at DATETIME(6)   NULL,
    updated_at DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ports_code (code),
    KEY idx_ports_country (country)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE cargo_categories (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120)  NOT NULL,
    description VARCHAR(500)  NULL,
    active      TINYINT(1)    NOT NULL DEFAULT 1,
    created_at  DATETIME(6)   NULL,
    updated_at  DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cargo_categories_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE cargo (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    name                VARCHAR(200)  NOT NULL,
    category_id         BIGINT        NOT NULL,
    description         TEXT          NULL,
    quantity            DECIMAL(18,4) NULL,
    unit                VARCHAR(30)   NULL,
    origin_country      VARCHAR(80)   NULL,
    destination_country VARCHAR(80)   NULL,
    loading_port_id     BIGINT        NULL,
    destination_port_id BIGINT        NULL,
    loading_date        DATE          NULL,
    estimated_arrival   DATE          NULL,
    indicative_price    DECIMAL(18,4) NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'INR',
    status              VARCHAR(40)   NOT NULL DEFAULT 'AVAILABLE',
    created_at          DATETIME(6)   NULL,
    updated_at          DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_cargo_category (category_id),
    KEY idx_cargo_loading_port (loading_port_id),
    KEY idx_cargo_destination_port (destination_port_id),
    KEY idx_cargo_status (status),
    CONSTRAINT fk_cargo_category FOREIGN KEY (category_id) REFERENCES cargo_categories (id),
    CONSTRAINT fk_cargo_loading_port FOREIGN KEY (loading_port_id) REFERENCES ports (id),
    CONSTRAINT fk_cargo_destination_port FOREIGN KEY (destination_port_id) REFERENCES ports (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE vessels (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    name               VARCHAR(200)  NOT NULL,
    imo_number         VARCHAR(20)   NOT NULL,
    vessel_type        VARCHAR(80)   NULL,
    capacity           DECIMAL(18,4) NULL,
    capacity_unit      VARCHAR(30)   NULL,
    flag               VARCHAR(80)   NULL,
    current_location   VARCHAR(200)  NULL,
    status             VARCHAR(40)   NOT NULL DEFAULT 'AVAILABLE',
    management_company VARCHAR(200)  NULL,
    management_contact VARCHAR(120)  NULL,
    description        TEXT          NULL,
    created_at         DATETIME(6)   NULL,
    updated_at         DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vessels_imo (imo_number),
    KEY idx_vessels_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE vessel_images (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    vessel_id  BIGINT       NOT NULL,
    public_id  VARCHAR(255) NULL,
    secure_url VARCHAR(500) NOT NULL,
    caption    VARCHAR(255) NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NULL,
    updated_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_vessel_images_vessel (vessel_id),
    CONSTRAINT fk_vessel_images_vessel FOREIGN KEY (vessel_id) REFERENCES vessels (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- ENQUIRIES & NEGOTIATIONS
-- -----------------------------------------------------------------------------

CREATE TABLE enquiries (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    reference_no           VARCHAR(30)   NOT NULL,
    client_id              BIGINT        NOT NULL,
    contact_name           VARCHAR(150)  NULL,
    contact_email          VARCHAR(150)  NULL,
    contact_phone          VARCHAR(30)   NULL,
    cargo_type             VARCHAR(200)  NOT NULL,
    cargo_category_id      BIGINT        NULL,
    cargo_description      TEXT          NULL,
    quantity               DECIMAL(18,4) NULL,
    unit                   VARCHAR(30)   NULL,
    origin_country         VARCHAR(80)   NULL,
    origin_location        VARCHAR(200)  NULL,
    loading_port_id        BIGINT        NULL,
    destination_country    VARCHAR(80)   NULL,
    destination_location   VARCHAR(200)  NULL,
    destination_port_id    BIGINT        NULL,
    required_loading_date  DATE          NULL,
    expected_delivery_date DATE          NULL,
    currency               VARCHAR(3)    NOT NULL DEFAULT 'INR',
    estimated_budget       DECIMAL(18,4) NULL,
    target_price_per_unit  DECIMAL(18,4) NULL,
    message                TEXT          NULL,
    status                 VARCHAR(40)   NOT NULL DEFAULT 'NEW',
    created_at             DATETIME(6)   NULL,
    updated_at             DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_enquiries_reference (reference_no),
    KEY idx_enquiries_client (client_id),
    KEY idx_enquiries_category (cargo_category_id),
    KEY idx_enquiries_status (status),
    CONSTRAINT fk_enquiries_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_enquiries_category FOREIGN KEY (cargo_category_id) REFERENCES cargo_categories (id),
    CONSTRAINT fk_enquiries_loading_port FOREIGN KEY (loading_port_id) REFERENCES ports (id),
    CONSTRAINT fk_enquiries_destination_port FOREIGN KEY (destination_port_id) REFERENCES ports (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE negotiations (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enquiry_id    BIGINT        NOT NULL,
    status        VARCHAR(40)   NOT NULL DEFAULT 'OPEN',
    agreed_price  DECIMAL(18,4) NULL,
    currency      VARCHAR(3)    NULL,
    opened_by_id  BIGINT        NOT NULL,
    closed_at     DATETIME(6)   NULL,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_negotiations_enquiry (enquiry_id),
    KEY idx_negotiations_opened_by (opened_by_id),
    CONSTRAINT fk_negotiations_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries (id),
    CONSTRAINT fk_negotiations_opened_by FOREIGN KEY (opened_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE negotiation_messages (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    negotiation_id BIGINT        NOT NULL,
    sender_id      BIGINT        NOT NULL,
    sender_type    VARCHAR(20)   NOT NULL,
    offer_price    DECIMAL(18,4) NULL,
    message        TEXT          NULL,
    status         VARCHAR(30)   NOT NULL DEFAULT 'PROPOSED',
    created_at     DATETIME(6)   NULL,
    updated_at     DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_negotiation_messages_thread (negotiation_id),
    KEY idx_negotiation_messages_sender (sender_id),
    CONSTRAINT fk_negotiation_messages_negotiation FOREIGN KEY (negotiation_id) REFERENCES negotiations (id),
    CONSTRAINT fk_negotiation_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- TAX CONFIGURATION
-- -----------------------------------------------------------------------------

CREATE TABLE tax_rates (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    name           VARCHAR(120)  NOT NULL,
    tax_type       VARCHAR(30)   NOT NULL,
    rate           DECIMAL(9,4)  NOT NULL,
    country        VARCHAR(80)   NOT NULL,
    jurisdiction   VARCHAR(120)  NULL,
    effective_from DATE          NOT NULL,
    active         TINYINT(1)    NOT NULL DEFAULT 1,
    description    VARCHAR(500)  NULL,
    created_at     DATETIME(6)   NULL,
    updated_at     DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_tax_rates_lookup (tax_type, country, active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- QUOTATIONS
-- -----------------------------------------------------------------------------

CREATE TABLE quotations (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    quote_no                VARCHAR(30)   NOT NULL,
    enquiry_id              BIGINT        NULL,
    client_id               BIGINT        NOT NULL,
    quotation_date          DATE          NOT NULL,
    valid_until             DATE          NULL,
    billing_address_line1   VARCHAR(255)  NULL,
    billing_address_line2   VARCHAR(255)  NULL,
    billing_city            VARCHAR(100)  NULL,
    billing_state           VARCHAR(100)  NULL,
    billing_postal_code     VARCHAR(20)   NULL,
    billing_country         VARCHAR(80)   NULL,
    shipping_address_line1  VARCHAR(255)  NULL,
    shipping_address_line2  VARCHAR(255)  NULL,
    shipping_city           VARCHAR(100)  NULL,
    shipping_state          VARCHAR(100)  NULL,
    shipping_postal_code    VARCHAR(20)   NULL,
    shipping_country        VARCHAR(80)   NULL,
    contact_email           VARCHAR(150)  NULL,
    contact_phone           VARCHAR(30)   NULL,
    gstin                   VARCHAR(20)   NULL,
    country                 VARCHAR(80)   NULL,
    currency                VARCHAR(3)    NOT NULL DEFAULT 'INR',
    incoterms               VARCHAR(20)   NULL,
    payment_terms           VARCHAR(1000) NULL,
    delivery_terms          VARCHAR(1000) NULL,
    notes                   TEXT          NULL,
    terms_conditions        TEXT          NULL,
    subtotal                DECIMAL(18,4) NOT NULL DEFAULT 0,
    discount                DECIMAL(18,4) NOT NULL DEFAULT 0,
    freight_charges         DECIMAL(18,4) NOT NULL DEFAULT 0,
    loading_charges         DECIMAL(18,4) NOT NULL DEFAULT 0,
    documentation_charges   DECIMAL(18,4) NOT NULL DEFAULT 0,
    insurance_charges       DECIMAL(18,4) NOT NULL DEFAULT 0,
    other_charges           DECIMAL(18,4) NOT NULL DEFAULT 0,
    taxable_amount          DECIMAL(18,4) NOT NULL DEFAULT 0,
    tax_treatment           VARCHAR(40)   NULL,
    tax_rate_id             BIGINT        NULL,
    tax_amount              DECIMAL(18,4) NOT NULL DEFAULT 0,
    grand_total             DECIMAL(18,4) NOT NULL DEFAULT 0,
    status                  VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    secure_token            VARCHAR(64)   NOT NULL,
    sent_at                 DATETIME(6)   NULL,
    viewed_at               DATETIME(6)   NULL,
    accepted_at             DATETIME(6)   NULL,
    rejected_at             DATETIME(6)   NULL,
    rejection_reason        VARCHAR(500)  NULL,
    created_by_id           BIGINT        NULL,
    created_at              DATETIME(6)   NULL,
    updated_at              DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_quotations_quote_no (quote_no),
    UNIQUE KEY uk_quotations_secure_token (secure_token),
    KEY idx_quotations_enquiry (enquiry_id),
    KEY idx_quotations_client (client_id),
    KEY idx_quotations_status (status),
    CONSTRAINT fk_quotations_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries (id),
    CONSTRAINT fk_quotations_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_quotations_tax_rate FOREIGN KEY (tax_rate_id) REFERENCES tax_rates (id),
    CONSTRAINT fk_quotations_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE quotation_items (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    quotation_id  BIGINT        NOT NULL,
    item_order    INT           NOT NULL DEFAULT 0,
    description   VARCHAR(500)  NOT NULL,
    quantity      DECIMAL(18,4) NOT NULL,
    unit          VARCHAR(30)   NULL,
    rate_per_unit DECIMAL(18,4) NOT NULL DEFAULT 0,
    line_amount   DECIMAL(18,4) NOT NULL DEFAULT 0,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_quotation_items_quotation (quotation_id),
    CONSTRAINT fk_quotation_items_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- PROFORMA INVOICES
-- -----------------------------------------------------------------------------

CREATE TABLE proforma_invoices (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    pi_no             VARCHAR(30)   NOT NULL,
    quotation_id      BIGINT        NULL,
    client_id         BIGINT        NOT NULL,
    issue_date        DATE          NOT NULL,
    valid_until       DATE          NULL,
    currency          VARCHAR(3)    NOT NULL DEFAULT 'INR',
    subtotal          DECIMAL(18,4) NOT NULL DEFAULT 0,
    discount          DECIMAL(18,4) NOT NULL DEFAULT 0,
    taxable_amount    DECIMAL(18,4) NOT NULL DEFAULT 0,
    tax_treatment     VARCHAR(40)   NULL,
    tax_amount        DECIMAL(18,4) NOT NULL DEFAULT 0,
    grand_total       DECIMAL(18,4) NOT NULL DEFAULT 0,
    payment_terms     VARCHAR(1000) NULL,
    bank_details      TEXT          NULL,
    notes             TEXT          NULL,
    status            VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    sent_at           DATETIME(6)   NULL,
    pdf_public_id     VARCHAR(255)  NULL,
    pdf_url           VARCHAR(500)  NULL,
    created_by_id     BIGINT        NULL,
    created_at        DATETIME(6)   NULL,
    updated_at        DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_proforma_pi_no (pi_no),
    KEY idx_proforma_quotation (quotation_id),
    KEY idx_proforma_client (client_id),
    KEY idx_proforma_status (status),
    CONSTRAINT fk_proforma_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id),
    CONSTRAINT fk_proforma_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_proforma_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE proforma_invoice_items (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    proforma_invoice_id BIGINT        NOT NULL,
    item_order          INT           NOT NULL DEFAULT 0,
    description         VARCHAR(500)  NOT NULL,
    quantity            DECIMAL(18,4) NOT NULL,
    unit                VARCHAR(30)   NULL,
    rate_per_unit       DECIMAL(18,4) NOT NULL DEFAULT 0,
    line_amount         DECIMAL(18,4) NOT NULL DEFAULT 0,
    created_at          DATETIME(6)   NULL,
    updated_at          DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_pi_items_proforma (proforma_invoice_id),
    CONSTRAINT fk_pi_items_proforma FOREIGN KEY (proforma_invoice_id) REFERENCES proforma_invoices (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- SHIPMENTS
-- -----------------------------------------------------------------------------

CREATE TABLE shipments (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    shipment_ref        VARCHAR(30)   NOT NULL,
    tracking_token      VARCHAR(64)   NOT NULL,
    client_id           BIGINT        NOT NULL,
    vessel_id           BIGINT        NULL,
    cargo_id            BIGINT        NULL,
    enquiry_id          BIGINT        NULL,
    quotation_id        BIGINT        NULL,
    proforma_invoice_id BIGINT        NULL,
    quantity            DECIMAL(18,4) NULL,
    unit                VARCHAR(30)   NULL,
    origin_country      VARCHAR(80)   NULL,
    destination_country VARCHAR(80)   NULL,
    loading_port_id     BIGINT        NULL,
    destination_port_id BIGINT        NULL,
    loading_date        DATE          NULL,
    estimated_arrival   DATE          NULL,
    actual_arrival      DATE          NULL,
    delivered_at        DATETIME(6)   NULL,
    final_price         DECIMAL(18,4) NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'INR',
    status              VARCHAR(40)   NOT NULL DEFAULT 'BOOKING_CONFIRMED',
    current_location    VARCHAR(200)  NULL,
    current_latitude    DECIMAL(10,7) NULL,
    current_longitude   DECIMAL(10,7) NULL,
    last_tracked_at     DATETIME(6)   NULL,
    notes               TEXT          NULL,
    created_by_id       BIGINT        NULL,
    created_at          DATETIME(6)   NULL,
    updated_at          DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shipments_ref (shipment_ref),
    UNIQUE KEY uk_shipments_tracking_token (tracking_token),
    KEY idx_shipments_client (client_id),
    KEY idx_shipments_vessel (vessel_id),
    KEY idx_shipments_cargo (cargo_id),
    KEY idx_shipments_status (status),
    CONSTRAINT fk_shipments_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_shipments_vessel FOREIGN KEY (vessel_id) REFERENCES vessels (id),
    CONSTRAINT fk_shipments_cargo FOREIGN KEY (cargo_id) REFERENCES cargo (id),
    CONSTRAINT fk_shipments_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries (id),
    CONSTRAINT fk_shipments_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id),
    CONSTRAINT fk_shipments_proforma FOREIGN KEY (proforma_invoice_id) REFERENCES proforma_invoices (id),
    CONSTRAINT fk_shipments_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE shipment_tracking (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    shipment_id    BIGINT        NOT NULL,
    status         VARCHAR(40)   NULL,
    location_label VARCHAR(200)  NULL,
    latitude       DECIMAL(10,7) NULL,
    longitude      DECIMAL(10,7) NULL,
    occurred_at    DATETIME(6)   NOT NULL,
    notes          TEXT          NULL,
    recorded_by_id BIGINT        NULL,
    created_at     DATETIME(6)   NULL,
    updated_at     DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_tracking_shipment (shipment_id),
    KEY idx_tracking_occurred (occurred_at),
    CONSTRAINT fk_tracking_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    CONSTRAINT fk_tracking_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- TAX INVOICES
-- -----------------------------------------------------------------------------

CREATE TABLE invoices (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    invoice_no              VARCHAR(30)   NOT NULL,
    invoice_type            VARCHAR(30)   NOT NULL DEFAULT 'TAX_INVOICE',
    client_id               BIGINT        NOT NULL,
    quotation_id            BIGINT        NULL,
    proforma_invoice_id     BIGINT        NULL,
    shipment_id             BIGINT        NULL,
    issue_date              DATE          NOT NULL,
    due_date                DATE          NULL,
    billing_address_line1   VARCHAR(255)  NULL,
    billing_address_line2   VARCHAR(255)  NULL,
    billing_city            VARCHAR(100)  NULL,
    billing_state           VARCHAR(100)  NULL,
    billing_postal_code     VARCHAR(20)   NULL,
    billing_country         VARCHAR(80)   NULL,
    shipping_address_line1  VARCHAR(255)  NULL,
    shipping_address_line2  VARCHAR(255)  NULL,
    shipping_city           VARCHAR(100)  NULL,
    shipping_state          VARCHAR(100)  NULL,
    shipping_postal_code    VARCHAR(20)   NULL,
    shipping_country        VARCHAR(80)   NULL,
    gstin                   VARCHAR(20)   NULL,
    pan                     VARCHAR(20)   NULL,
    place_of_supply         VARCHAR(120)  NULL,
    currency                VARCHAR(3)    NOT NULL DEFAULT 'INR',
    exchange_rate           DECIMAL(14,6) NULL,
    incoterms               VARCHAR(20)   NULL,
    port_of_loading         VARCHAR(150)  NULL,
    port_of_discharge       VARCHAR(150)  NULL,
    export_reference        VARCHAR(100)  NULL,
    subtotal                DECIMAL(18,4) NOT NULL DEFAULT 0,
    discount                DECIMAL(18,4) NOT NULL DEFAULT 0,
    freight_charges         DECIMAL(18,4) NOT NULL DEFAULT 0,
    loading_charges         DECIMAL(18,4) NOT NULL DEFAULT 0,
    documentation_charges   DECIMAL(18,4) NOT NULL DEFAULT 0,
    insurance_charges       DECIMAL(18,4) NOT NULL DEFAULT 0,
    other_charges           DECIMAL(18,4) NOT NULL DEFAULT 0,
    additional_charges      DECIMAL(18,4) NOT NULL DEFAULT 0,
    taxable_amount          DECIMAL(18,4) NOT NULL DEFAULT 0,
    cgst_amount             DECIMAL(18,4) NOT NULL DEFAULT 0,
    sgst_amount             DECIMAL(18,4) NOT NULL DEFAULT 0,
    igst_amount             DECIMAL(18,4) NOT NULL DEFAULT 0,
    other_tax_amount        DECIMAL(18,4) NOT NULL DEFAULT 0,
    total_tax_amount        DECIMAL(18,4) NOT NULL DEFAULT 0,
    grand_total             DECIMAL(18,4) NOT NULL DEFAULT 0,
    paid_amount             DECIMAL(18,4) NOT NULL DEFAULT 0,
    balance_amount          DECIMAL(18,4) NOT NULL DEFAULT 0,
    tax_treatment           VARCHAR(40)   NULL,
    payment_terms           VARCHAR(1000) NULL,
    bank_details            TEXT          NULL,
    notes                   TEXT          NULL,
    terms_conditions        TEXT          NULL,
    status                  VARCHAR(30)   NOT NULL DEFAULT 'ISSUED',
    sent_at                 DATETIME(6)   NULL,
    pdf_public_id           VARCHAR(255)  NULL,
    pdf_url                 VARCHAR(500)  NULL,
    created_by_id           BIGINT        NULL,
    created_at              DATETIME(6)   NULL,
    updated_at              DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoices_invoice_no (invoice_no),
    KEY idx_invoices_client (client_id),
    KEY idx_invoices_quotation (quotation_id),
    KEY idx_invoices_proforma (proforma_invoice_id),
    KEY idx_invoices_shipment (shipment_id),
    KEY idx_invoices_status (status),
    CONSTRAINT fk_invoices_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_invoices_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id),
    CONSTRAINT fk_invoices_proforma FOREIGN KEY (proforma_invoice_id) REFERENCES proforma_invoices (id),
    CONSTRAINT fk_invoices_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    CONSTRAINT fk_invoices_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE invoice_items (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    invoice_id    BIGINT        NOT NULL,
    item_order    INT           NOT NULL DEFAULT 0,
    description   VARCHAR(500)  NOT NULL,
    hsn_code      VARCHAR(20)   NULL,
    quantity      DECIMAL(18,4) NOT NULL,
    unit          VARCHAR(30)   NULL,
    rate_per_unit DECIMAL(18,4) NOT NULL DEFAULT 0,
    line_amount   DECIMAL(18,4) NOT NULL DEFAULT 0,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_invoice_items_invoice (invoice_id),
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- PAYMENTS, TRANSACTIONS, WEBHOOKS, RECEIPTS, REFUNDS
-- -----------------------------------------------------------------------------

CREATE TABLE payments (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    client_id             BIGINT        NOT NULL,
    invoice_id            BIGINT        NULL,
    proforma_invoice_id   BIGINT        NULL,
    payment_type          VARCHAR(30)   NOT NULL DEFAULT 'ADVANCE',
    method                VARCHAR(30)   NOT NULL DEFAULT 'RAZORPAY',
    status                VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    amount                DECIMAL(18,4) NOT NULL,
    currency              VARCHAR(3)    NOT NULL DEFAULT 'INR',
    paid_at               DATETIME(6)   NULL,
    transaction_reference VARCHAR(100)  NULL,
    razorpay_order_id     VARCHAR(64)   NULL,
    notes                 TEXT          NULL,
    recorded_by_id        BIGINT        NULL,
    created_at            DATETIME(6)   NULL,
    updated_at            DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_payments_client (client_id),
    KEY idx_payments_invoice (invoice_id),
    KEY idx_payments_proforma (proforma_invoice_id),
    KEY idx_payments_status (status),
    CONSTRAINT fk_payments_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT fk_payments_proforma FOREIGN KEY (proforma_invoice_id) REFERENCES proforma_invoices (id),
    CONSTRAINT fk_payments_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_transactions (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    payment_id          BIGINT        NOT NULL,
    gateway             VARCHAR(30)   NOT NULL DEFAULT 'RAZORPAY',
    razorpay_order_id   VARCHAR(64)   NULL,
    razorpay_payment_id VARCHAR(64)   NULL,
    razorpay_signature  VARCHAR(255)  NULL,
    amount              DECIMAL(18,4) NOT NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'INR',
    status              VARCHAR(30)   NOT NULL DEFAULT 'CREATED',
    error_description   VARCHAR(500)  NULL,
    raw_response        TEXT          NULL,
    captured_at         DATETIME(6)   NULL,
    created_at          DATETIME(6)   NULL,
    updated_at          DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ptx_razorpay_order (razorpay_order_id),
    UNIQUE KEY uk_ptx_razorpay_payment (razorpay_payment_id),
    KEY idx_ptx_payment (payment_id),
    CONSTRAINT fk_ptx_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_webhooks (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    event_id     VARCHAR(100)  NOT NULL,
    event_type   VARCHAR(100)  NULL,
    payload      TEXT          NULL,
    signature    VARCHAR(255)  NULL,
    processed    TINYINT(1)    NOT NULL DEFAULT 0,
    received_at  DATETIME(6)   NOT NULL,
    processed_at DATETIME(6)   NULL,
    created_at   DATETIME(6)   NULL,
    updated_at   DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_webhooks_event_id (event_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE receipts (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    receipt_no             VARCHAR(30)   NOT NULL,
    payment_id             BIGINT        NOT NULL,
    client_id              BIGINT        NOT NULL,
    invoice_id             BIGINT        NULL,
    issued_on              DATE          NOT NULL,
    amount                 DECIMAL(18,4) NOT NULL,
    currency               VARCHAR(3)    NOT NULL DEFAULT 'INR',
    method                 VARCHAR(30)   NULL,
    gateway_transaction_id VARCHAR(100)  NULL,
    remaining_balance      DECIMAL(18,4) NOT NULL DEFAULT 0,
    notes                  VARCHAR(500)  NULL,
    pdf_public_id          VARCHAR(255)  NULL,
    pdf_url                VARCHAR(500)  NULL,
    created_at             DATETIME(6)   NULL,
    updated_at             DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_receipts_receipt_no (receipt_no),
    KEY idx_receipts_payment (payment_id),
    KEY idx_receipts_client (client_id),
    KEY idx_receipts_invoice (invoice_id),
    CONSTRAINT fk_receipts_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_receipts_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_receipts_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE refunds (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    payment_id        BIGINT        NOT NULL,
    amount            DECIMAL(18,4) NOT NULL,
    refund_type       VARCHAR(20)   NOT NULL DEFAULT 'FULL',
    reason            VARCHAR(500)  NULL,
    gateway_refund_id VARCHAR(100)  NULL,
    status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    refunded_at       DATETIME(6)   NULL,
    processed_by_id   BIGINT        NULL,
    notes             TEXT          NULL,
    created_at        DATETIME(6)   NULL,
    updated_at        DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_refunds_payment (payment_id),
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_refunds_processed_by FOREIGN KEY (processed_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- DOCUMENTS, NOTIFICATIONS, REVIEWS, CONTACT
-- -----------------------------------------------------------------------------

CREATE TABLE documents (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    owner_type       VARCHAR(40)  NOT NULL,
    owner_id         BIGINT       NOT NULL,
    category         VARCHAR(40)  NOT NULL,
    title            VARCHAR(200) NULL,
    public_id        VARCHAR(255) NULL,
    secure_url       VARCHAR(500) NOT NULL,
    file_format      VARCHAR(20)  NULL,
    file_size_bytes  BIGINT       NULL,
    uploaded_by_id   BIGINT       NULL,
    created_at       DATETIME(6)  NULL,
    updated_at       DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_documents_owner (owner_type, owner_id),
    KEY idx_documents_uploaded_by (uploaded_by_id),
    CONSTRAINT fk_documents_uploaded_by FOREIGN KEY (uploaded_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    user_id          BIGINT        NOT NULL,
    notification_type VARCHAR(40)  NULL,
    title            VARCHAR(200)  NOT NULL,
    message          VARCHAR(1000) NULL,
    link             VARCHAR(300)  NULL,
    entity_type      VARCHAR(40)   NULL,
    entity_id        BIGINT        NULL,
    is_read          TINYINT(1)    NOT NULL DEFAULT 0,
    read_at          DATETIME(6)   NULL,
    created_at       DATETIME(6)   NULL,
    updated_at       DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_user_read (user_id, is_read),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE reviews (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    shipment_id   BIGINT        NOT NULL,
    client_id     BIGINT        NOT NULL,
    rating        INT           NOT NULL,
    title         VARCHAR(150)  NULL,
    review_text   TEXT          NULL,
    approved      TINYINT(1)    NOT NULL DEFAULT 0,
    moderated_by_id BIGINT      NULL,
    moderated_at  DATETIME(6)   NULL,
    created_at    DATETIME(6)   NULL,
    updated_at    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_shipment (shipment_id),
    KEY idx_reviews_client (client_id),
    KEY idx_reviews_approved (approved),
    CONSTRAINT fk_reviews_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    CONSTRAINT fk_reviews_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_reviews_moderated_by FOREIGN KEY (moderated_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE contact_messages (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    full_name    VARCHAR(150)  NOT NULL,
    email        VARCHAR(150)  NOT NULL,
    phone        VARCHAR(30)   NULL,
    company      VARCHAR(200)  NULL,
    subject      VARCHAR(255)  NULL,
    message      TEXT          NOT NULL,
    handled      TINYINT(1)    NOT NULL DEFAULT 0,
    handled_by_id BIGINT       NULL,
    handled_at   DATETIME(6)   NULL,
    created_at   DATETIME(6)   NULL,
    updated_at   DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_contact_handled (handled),
    CONSTRAINT fk_contact_handled_by FOREIGN KEY (handled_by_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- REUSABLE ADDRESSES
-- -----------------------------------------------------------------------------

CREATE TABLE billing_addresses (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    client_id      BIGINT       NOT NULL,
    label          VARCHAR(50)  NULL,
    address_line1  VARCHAR(255) NOT NULL,
    address_line2  VARCHAR(255) NULL,
    city           VARCHAR(100) NULL,
    state          VARCHAR(100) NULL,
    postal_code    VARCHAR(20)  NULL,
    country        VARCHAR(80)  NULL,
    gstin          VARCHAR(20)  NULL,
    default_address TINYINT(1)  NOT NULL DEFAULT 0,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_billing_client (client_id),
    CONSTRAINT fk_billing_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE shipping_addresses (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    client_id      BIGINT       NOT NULL,
    label          VARCHAR(50)  NULL,
    address_line1  VARCHAR(255) NOT NULL,
    address_line2  VARCHAR(255) NULL,
    city           VARCHAR(100) NULL,
    state          VARCHAR(100) NULL,
    postal_code    VARCHAR(20)  NULL,
    country        VARCHAR(80)  NULL,
    default_address TINYINT(1)  NOT NULL DEFAULT 0,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_shipping_client (client_id),
    CONSTRAINT fk_shipping_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- SEQUENCES & AUDIT
-- -----------------------------------------------------------------------------

CREATE TABLE document_sequences (
    doc_type VARCHAR(50) NOT NULL,
    doc_year INT NOT NULL,
    last_number BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (doc_type, doc_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor_id    BIGINT       NULL,
    actor_email VARCHAR(150) NULL,
    action      VARCHAR(60)  NOT NULL,
    entity_type VARCHAR(60)  NOT NULL,
    entity_id   BIGINT       NULL,
    old_value   TEXT         NULL,
    new_value   TEXT         NULL,
    ip_address  VARCHAR(45)  NULL,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_audit_actor (actor_id),
    KEY idx_audit_entity (entity_type, entity_id),
    KEY idx_audit_created (created_at),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
