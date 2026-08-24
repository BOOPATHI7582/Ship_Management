-- =============================================================================
-- ExportPlatform - Seed Data (Phase 2)
--
-- Only configuration/master data is seeded here:
--   * Roles (initial set per spec)
--   * Cargo categories (dynamic taxonomy starters)
--   * Ports (major Indian + international gateways, UN/LOCODE codes)
--   * Tax rate CONFIGURATION ROWS (editable/deactivatable by admin;
--     business logic never hard-codes these values)
--
-- No users are seeded: registration/bootstrap happens in Phase 3.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ROLES (initial set)
-- -----------------------------------------------------------------------------
INSERT INTO roles (name, description) VALUES
('CLIENT',       'Client account - submits export requirements, receives quotations/invoices, pays'),
('ADMIN',        'Platform administrator - full commercial and operational control'),
('SHIP_MANAGER', 'Ship manager - vessels, cargo and shipment operations');

-- -----------------------------------------------------------------------------
-- CARGO CATEGORIES
-- -----------------------------------------------------------------------------
INSERT INTO cargo_categories (name, description, active) VALUES
('Iron Ore',              'Iron ore fines, lumps and pellets', 1),
('Coal',                  'Thermal and coking coal', 1),
('Steel',                 'Steel products, billets, coils and scrap', 1),
('Cement',                'Bulk and bagged cement/clinker', 1),
('Rice',                  'Basmati and non-basmati rice', 1),
('Wheat',                 'Wheat and wheat products', 1),
('Agricultural Products', 'Grains, pulses, spices and agro commodities', 1),
('Minerals',              'Bauxite, gypsum, limestone and other minerals', 1),
('Chemicals',             'Industrial chemicals and petrochemicals', 1),
('Machinery',             'Heavy machinery, equipment and project cargo', 1),
('General Cargo',         'Break-bulk and general merchandise', 1),
('Bulk Cargo',            'Dry bulk commodities', 1),
('Container Cargo',       'FCL/LCL containerized shipments', 1);

-- -----------------------------------------------------------------------------
-- PORTS (name, country, city, UN/LOCODE, latitude, longitude)
-- -----------------------------------------------------------------------------
INSERT INTO ports (name, country, city, code, latitude, longitude, active) VALUES
('Mundra Port',             'India',                 'Mundra',        'INMUN', 22.7396000,  69.7025000, 1),
('Deendayal (Kandla) Port', 'India',                 'Gandhidham',    'INKDL', 23.0200000,  70.2200000, 1),
('Nhava Sheva (JNPT)',      'India',                 'Navi Mumbai',   'INNSA', 18.9494000,  72.9511000, 1),
('Mumbai Port',             'India',                 'Mumbai',        'INBOM', 18.9256000,  72.8353000, 1),
('Chennai Port',            'India',                 'Chennai',       'INMAA', 13.0827000,  80.2707000, 1),
('Visakhapatnam Port',      'India',                 'Visakhapatnam', 'INVTZ', 17.6868000,  83.2185000, 1),
('Kolkata Port',            'India',                 'Kolkata',       'INCCU', 22.5726000,  88.3639000, 1),
('Paradip Port',            'India',                 'Paradip',       'INPAR', 20.3166000,  86.6096000, 1),
('Kochi Port',              'India',                 'Kochi',         'INCOK',  9.9312000,  76.2673000, 1),
('Mormugao Port',           'India',                 'Mormugao',      'INGOI', 15.4008000,  73.8006000, 1),
('Jebel Ali Port',          'United Arab Emirates',  'Dubai',         'AEJEA', 25.0111000,  55.0611000, 1),
('Fujairah Port',           'United Arab Emirates',  'Fujairah',      'AEFJR', 25.1213000,  56.3533000, 1),
('Port of Singapore',       'Singapore',             'Singapore',     'SGSIN',  1.2644000, 103.8220000, 1),
('Shanghai Port',           'China',                 'Shanghai',      'CNSHA', 31.2304000, 121.4737000, 1),
('Port Klang',              'Malaysia',              'Klang',         'MYPKG',  3.0319000, 101.3936000, 1),
('Colombo Port',            'Sri Lanka',             'Colombo',       'LKCMB',  6.9271000,  79.8612000, 1),
('Rotterdam Port',          'Netherlands',           'Rotterdam',     'NLRTM', 51.9244000,   4.4777000, 1),
('Hamburg Port',            'Germany',               'Hamburg',       'DEHAM', 53.5511000,   9.9937000, 1),
('Felixstowe Port',         'United Kingdom',        'Felixstowe',    'GBFXT', 51.9539000,   1.3138000, 1),
('Houston Port',            'United States',         'Houston',       'USHOU', 29.7604000, -95.3698000, 1);

-- -----------------------------------------------------------------------------
-- TAX RATES (configurable defaults - admin manages via Tax module)
-- NOTE: These are configuration rows, not hard-coded logic. Final invoice
-- formats/treatments should be validated with a qualified tax professional.
-- -----------------------------------------------------------------------------
INSERT INTO tax_rates (name, tax_type, rate, country, jurisdiction, effective_from, active, description) VALUES
('IGST - Domestic Services',        'IGST',        18.0000, 'India', 'Inter-State',        '2017-07-01', 1, 'Integrated GST on inter-state supply of services (editable default)'),
('CGST - Intra-State',              'CGST',         9.0000, 'India', 'Intra-State',        '2017-07-01', 1, 'Central GST half of intra-state split (editable default)'),
('SGST - Intra-State',              'SGST',         9.0000, 'India', 'Intra-State',        '2017-07-01', 1, 'State GST half of intra-state split (editable default)'),
('Export - Zero Rated (LUT)',       'ZERO_RATED',   0.0000, 'India', 'Export under LUT',   '2017-07-01', 1, 'Zero-rated export supply under Letter of Undertaking (editable default)');
