# ExportPlatform — System Architecture

Global Export, Cargo, Vessel, Shipment, Quotation, Tax Invoice, Payment, Billing & Tracking Management Platform.
Stack: **React.js (Vite) + Java Spring Boot + MySQL 8**, Cloudinary storage, Razorpay payments (India deployment).

---

## 1. Final System Architecture

```
┌─────────────────────────────┐         ┌──────────────────────────────────┐
│  React SPA (Vite, JS)       │  HTTPS  │  Spring Boot REST API (:8080)    │
│  - Public website           │◄───────►│  - Spring Security + JWT filter  │
│  - Client dashboard         │  JSON   │  - Controllers → Services        │
│  - Admin dashboard          │         │  - JPA/Hibernate repositories    │
│  Tailwind + Framer Motion   │         │  - BigDecimal finance engine     │
│  Recharts + Leaflet         │         └───────┬───────────┬──────────────┘
└─────────────────────────────┘                 │           │
                                                ▼           ▼
                                     ┌──────────────┐   ┌─────────────────────┐
                                     │  MySQL 8     │   │  Integrations       │
                                     │  :3306       │   │  - Razorpay API     │
                                     │  InnoDB      │   │  - Razorpay webhook │
                                     │  FKs/indexes │   │  - Cloudinary API   │
                                     │  DECIMAL(…)  │   │  - SMTP (Spring     │
                                     └──────────────┘   │    Boot Mail)       │
                                                        └─────────────────────┘
```

- Single-page React app talks **only** to the REST API; no server-side rendering.
- All money math (`BigDecimal`), numbering, tax, and payment verification happen in Java.
- React never sees secrets; only public Razorpay Key ID reaches the browser.

---

## 2. Frontend Architecture

| Concern | Choice |
|---|---|
| Build | Vite + React 18, plain JavaScript |
| Routing | `react-router-dom` v6 — public / auth / client / admin route trees |
| HTTP | Axios instance with JWT interceptor + refresh-on-401 handling |
| Styling | Tailwind CSS, custom maritime theme |
| Animation | Framer Motion (hero, scroll reveals, page transitions) |
| Charts | Recharts (admin/client dashboards) |
| Maps | Leaflet + OpenStreetMap tiles (tracking page) |
| State | React Context (`AuthContext`, `NotificationContext`) + hooks |
| UX | Toasts, skeleton loaders, empty/error states, confirm dialogs |

Route groups:

- `/` public site: Home, About, Services, Export Solutions, Cargo Categories, Available Shipments, How It Works, Tracking, Contact, Careers
- `/login`, `/register`, `/forgot-password`, `/reset-password`
- `/app/**` client dashboard (guarded: `CLIENT`)
- `/admin/**` admin dashboard (guarded: `ADMIN`, `SHIP_MANAGER` subsets)
- `/quotation/:secureToken` public secure quotation view (token-based)

---

## 3. Spring Boot Backend Architecture

Layered, per mandated package structure:

```
com.company.exportplatform
├── config/         SecurityConfig, CorsConfig, CloudinaryConfig,
│                   RazorpayConfig, MailConfig, OpenApiConfig, DataSeeder
├── controller/     AuthController, EnquiryController, QuotationController, …
├── dto/            request/ + response/ records (never expose entities)
├── entity/         JPA entities (BigDecimal money, enums for statuses)
├── repository/     Spring Data JPA + JpaSpecificationExecutor
├── service/        interfaces
├── service/impl/   business logic (@Transactional at service boundary)
├── security/       JwtService, JwtAuthFilter, UserDetailsServiceImpl,
│                   SecurityExpressions
├── exception/      GlobalExceptionHandler (@RestControllerAdvice),
│                   ApiException, ResourceNotFoundException
├── validation/     custom Bean Validation annotations
├── mapper/         MapStruct mappers (entity ↔ DTO)
├── specification/  reusable JPA Specifications (search/filter/sort)
├── payment/        RazorpayService, WebhookVerifier, IdempotencyGuard
├── email/          EmailService + Thymeleaf-style templates
├── pdf/            PdfGenerator (OpenPDF): QUO / PI / INV / REC layouts
├── cloudinary/     CloudinaryService (upload/sign/delete, metadata only in DB)
├── audit/          AuditService + AOP aspect for financial actions
├── notification/   NotificationService (in-app), wired into services
└── util/           DocumentNumberGenerator, MoneyUtils, DateUtils
```

Rules enforced by design:

- Controllers thin: validate → delegate → map DTO.
- Service layer owns transactions; repositories never called from controllers.
- Global error envelope: `{ success, message, timestamp, status }`.
- Backend pagination everywhere lists can grow (`Pageable`).

---

## 4. MySQL ER Relationship Design (core chain)

```
roles 1──* users 1──1 clients
                        │
                        ▼
              enquiries *──1 users(client)
                        │
              negotiations 1──* enquiry ──* negotiation_messages
                        │
                        ▼
              quotations *──1 enquiry ──* quotation_items
                        │            └──* tax_rates (applied treatment)
                        ▼
        proforma_invoices 1──1 quotation ──* proforma_invoice_items
                        │
                        ▼
              payments *──1 proforma_invoice / invoice
                        │
        payment_transactions *──1 payment
        payment_webhooks (event_id UNIQUE — idempotency)
        receipts *──1 payment      refunds *──1 payment
                        │
                        ▼
              shipments *──1 client, *──1 vessel, *──1 cargo
                │            ├──* ports (load/discharge)
                │            └──* shipment_tracking (manual points/timeline)
                ▼
              invoices (final tax invoice) ──* invoice_items
```

Side tables: `documents` (owner type+id), `notifications`, `reviews`,
`contact_messages`, `billing_addresses` / `shipping_addresses`,
`document_sequences` (pessimistic-locked counters), `audit_logs`, `password_resets`.

---

## 5. Database Table / Entity List

| Table | Purpose | Notable columns/constraints |
|---|---|---|
| roles | CLIENT, ADMIN, SHIP_MANAGER (+future) | `name` UNIQUE |
| users | login identity | `email` UNIQUE, `password_hash` BCrypt, `role_id` FK |
| clients | client profile | `user_id` FK UNIQUE |
| vessels | fleet | `imo_number` UNIQUE, sensitive mgmt fields role-gated |
| vessel_images | gallery | `vessel_id` FK, cloud URL |
| cargo_categories | dynamic taxonomy | `name` UNIQUE, active flag |
| cargo | inventory listings | FKs: category, load/discharge port; DECIMAL qty/price |
| ports | reusable ports | `code` UNIQUE, lat/lng DECIMAL(10,7) |
| enquiries | export requirements | `reference_no` UNIQUE, status enum, budget DECIMAL |
| negotiations | one per enquiry thread | status enum |
| negotiation_messages | immutable offers | sender, price DECIMAL, timestamp |
| quotations | commercial quotes | `quote_no` UNIQUE, secure_token UNIQUE, totals DECIMAL |
| quotation_items | line items | `quotation_id` FK |
| tax_rates | configurable taxes | name/type/rate/country/effective_date, active |
| proforma_invoices | PI docs | `pi_no` UNIQUE, bank snapshot |
| proforma_invoice_items | PI lines | FK |
| invoices | tax/final invoices | `invoice_no` UNIQUE, CGST/SGST/IGST DECIMAL |
| invoice_items | invoice lines | FK |
| payments | payment intents/records | balance calc fields, method enum |
| payment_transactions | gateway rows | `razorpay_order_id`/`payment_id` UNIQUE |
| payment_webhooks | raw events | `event_id` UNIQUE (idempotency) |
| receipts | REC docs | `receipt_no` UNIQUE |
| refunds | refunds | FK payment |
| shipments | operational shipments | status enum (11 states), FKs client/vessel/cargo |
| shipment_tracking | manual tracking log | lat/lng, location, notes, recorded_at |
| documents | metadata for files | owner_type + owner_id, cloud public_id/url |
| notifications | in-app alerts | `user_id` FK, read flag |
| reviews | post-delivery ratings | FK shipment (COMPLETED only), approved flag |
| contact_messages | public contact form | — |
| billing_addresses / shipping_addresses | reusable addresses | FK client |
| document_sequences | number generator | `doc_type+year` PK, `last_value`, row lock |
| audit_logs | financial history | user, action, entity, old/new JSON |
| password_resets | reset tokens | hashed token, expiry |

All engines InnoDB, `DECIMAL(18,4)` money, `DECIMAL(18,4)` quantities where fractional,
indexes on every FK plus common search columns, `ON DELETE RESTRICT` on financial chains.

---

## 6. API Architecture

Base: `/api`. Versioned later if needed. Standard envelope + `Pageable` params.

| Module | Endpoints |
|---|---|
| Auth | POST `/auth/register`, `/auth/login`, `/auth/forgot-password`, `/auth/reset-password` |
| Public | GET `/public/shipments`, `/public/cargo-categories`, `/public/reviews`, POST `/public/contact`, GET `/public/tracking/{shipmentRef}` |
| Users | GET `/users`, GET `/users/{id}` (ADMIN) |
| Master data | CRUD `/vessels`, `/cargo`, `/cargo-categories`, `/ports` (ADMIN; GET also SHIP_MANAGER) |
| Enquiries | POST/GET `/enquiries`, GET/PUT `/enquiries/{id}` (ownership-scoped for CLIENT) |
| Negotiations | POST `/negotiations/{enquiryId}/start`, POST `/negotiations/{id}/messages`, GET `/negotiations/{id}` |
| Quotations | POST `/quotations`, GET `/quotations`, PUT `/quotations/{id}`, POST `/quotations/{id}/send`, POST `/quotations/{id}/accept`, POST `/quotations/{id}/reject`; GET `/quotation-view/{secureToken}` |
| Proforma | POST/GET `/proforma-invoices`, GET `/{id}`, POST `/{id}/send` |
| Invoices | POST/GET `/invoices`, GET `/{id}`, POST `/{id}/send` |
| Payments | POST `/payments/create-order`, `/payments/verify`, `/payments/webhook` (public), GET `/payments` |
| Offline pay | POST `/payments/offline` (ADMIN) |
| Refunds | POST `/refunds` (ADMIN) |
| Receipts | GET `/receipts`, `/{id}` |
| Shipments | CRUD `/shipments`, GET/POST `/shipments/{id}/tracking` |
| Reviews | POST `/reviews`, GET `/reviews`, PUT `/reviews/{id}/moderate` (ADMIN) |
| Notifications | GET `/notifications`, PUT `/notifications/{id}/read`, PUT `/notifications/read-all` |
| Documents | POST `/documents/upload`, GET `/documents`, GET `/{id}/download-url` |
| Dashboards | GET `/dashboard/client`, GET `/dashboard/admin`, GET `/billing/dashboard` |

Access matrix enforced by `@PreAuthorize`: ADMIN > SHIP_MANAGER (ops-only) > CLIENT (own data only).

---

## 7. Authentication Architecture

- Register → validate (Bean Validation) → BCrypt hash → role CLIENT → welcome email → JWT issued.
- Login → authenticate → access token JWT (HS256, configurable expiry).
- `JwtAuthFilter` parses `Authorization: Bearer`, loads `UserDetails`, sets `SecurityContext`.
- Roles from DB authorities; method security via `@PreAuthorize`.
- Forgot password → single-use hashed token (30 min) emailed; reset updates hash + revokes tokens.
- Frontend: AuthContext stores token in memory + localStorage, Axios interceptor attaches it, route guards redirect by role.

---

## 8. Payment Architecture (Razorpay)

```
React "Pay Now"
   │ POST /api/payments/create-order {invoiceId}
   ▼
Backend: validates ownership/balance → Razorpay orders.create()
         → persists payment (PENDING) + transaction row → returns orderId + public key
React: opens Razorpay Checkout
After checkout:
   │ POST /api/payments/verify {orderId, paymentId, signature}
   ▼
Backend: HMAC-SHA256(orderId|paymentId, KEY_SECRET) == signature ?
         yes → mark PAID/SUCCESS inside @Transactional, update invoice paid/balance/status,
               generate receipt PDF, email, notify, audit
Webhook (backup, source of truth):
   POST /api/payments/webhook → verify X-Razorpay-Signature (WEBHOOK_SECRET)
   → insert payment_webhooks(event_id UNIQUE); duplicate ⇒ skip (idempotent)
   → reconcile payment/invoice/receipt exactly as verify path
```

Never trust frontend success; card data never touches our servers; offline payments (NEFT/RTGS/etc.) entered by ADMIN require proof doc + audit entry.

---

## 9. Tax Architecture

- `tax_rates` table: name, type (CGST/SGST/IGST/EXEMPT/ZERO_RATED/CUSTOM), rate, country, jurisdiction, effective_from, active.
- Per-document **tax treatment selection** by authorized admin (domestic intra/inter-state vs export zero-rated etc.) — nothing hard-coded.
- Calculation service (single source of truth):

```
subtotal = Σ(line.rate × line.qty)                    [BigDecimal]
taxable  = subtotal − discount + freight + loading + documentation + insurance + other
tax      = taxable × applicable rates (split CGST/SGST or IGST per treatment)
grand    = taxable + tax
```

- Stored per invoice (rate snapshot at issue time); historical docs never recalculated retroactively.
- Disclaimer surfaced in settings: final formats to be validated by a CA before production invoicing.

---

## 10. Quotation → Invoice Workflow

```
ENQUIRY(New→Reviewing→Contacted→Negotiating)
   → NEGOTIATION (append-only messages/offers)
   → QUOTATION (Draft→Sent→Viewed→Negotiating→Accepted/Rejected/Expired)
        send = generate PDF (OpenPDF) → store ref → email w/ attachment + secure link
        accept = timestamp + actor saved, status Accepted, notify admin, audit
   → PROFORMA INVOICE (PI-YYYY-nnnnnn) → advance payment expected
   → PAYMENT verified (advance %)
   → SHIPMENT created (Booking Confirmed … Delivered/Completed, 11 states)
   → FINAL TAX INVOICE (INV-YYYY-nnnnnn, final quantity/charges, advance adjusted)
   → BALANCE payment → RECEIPT (REC-YYYY-nnnnnn) → FINAL BILL (PAID)
   → DELIVERY → REVIEW allowed only when shipment COMPLETED
```

Numbering: `document_sequences` row locked (`PESSIMISTIC_WRITE`) per type/year → `QUO-2026-000001`, gaps possible, duplicates impossible. Financial documents are cancelled/versioned, never deleted.

---

## 11. Shipment Tracking Architecture

- `shipments` holds lifecycle state machine (11 statuses, forward-only transitions validated server-side).
- `shipment_tracking` rows appended by ADMIN/SHIP_MANAGER: lat, lng, location label, status, notes, occurred_at.
- Client/public view: latest position + full timeline; Leaflet map plots origin port, destination port, current point.
- Interface `TrackingProvider` implemented by `ManualTrackingProvider` today — future AIS/live feed plugs in behind the same interface without schema change.
- Public tracking exposes only non-sensitive fields via shipment reference token.

---

## 12. Cloudinary Architecture

- Server-side signed uploads only (API secret stays in backend env).
- Flow: backend issues signature → React uploads directly OR backend proxies upload → returns `public_id`, `secure_url`, bytes/format → stored in `documents` (MySQL keeps metadata + URLs only, never blobs).
- Transformations (f_auto,q_auto) for images; restricted delivery for private docs (signed URLs with expiry for invoices/contracts).
- Deletion only allowed when no financial document references remain (soft-delete + audit otherwise).

---

## 13. Email Architecture

- Spring Boot Mail (SMTP) + provider (e.g., Brevo/SendGrid SMTP) behind `EmailService`.
- Async (`@Async`) with retry + dead-log table so mail failure never rolls back business transactions.
- Templates: registration/welcome, enquiry received, quotation sent/viewed/accepted, negotiation offer, PI, invoice, payment success, receipt, shipment updates, final bill, contact acknowledgement, password reset.
- Every transactional email logs subject/to/reference entity for traceability.

---

## 14. Security Architecture

| Layer | Controls |
|---|---|
| Transport | HTTPS enforced in prod; HSTS at proxy |
| AuthN | JWT HS256 (256-bit secret in env), short expiry, BCrypt(10+) hashes |
| AuthZ | Role hierarchy + `@PreAuthorize`; object-level checks (client sees own rows only) |
| Input | Bean Validation on every DTO; JPA parameterized queries (no SQLi); strict content types |
| Output | DTO-only responses; no stack traces; sensitive vessel mgmt fields stripped by role |
| Payments | Signature verify (checkout + webhook), idempotency keys, no PAN/CVV storage |
| Abuse | Rate limiting on auth/contact endpoints, account lockout counters, CORS allow-list |
| Auditing | Audit logs (actor, action, entity, old/new, IP, ts) on every financial mutation |
| Config | Secrets only via environment variables; `.env` git-ignored; prod uses secret manager |

---

## 15. Complete Project Folder Structure

```
D:\Projects\ExportPlatform\
├── MASTER_PROMPT.md
├── ARCHITECTURE.md
├── README.md
├── .gitignore
├── backend\                          ← Spring Boot (Maven)
│   ├── pom.xml
│   └── src\main\java\com\company\exportplatform\…   (§3 packages)
│   └── src\main\resources\
│       ├── application.yml           (env-driven)
│       ├── application-dev.yml / application-prod.yml
│       ├── db\migration\             Flyway SQL migrations
│       └── templates\email\…
├── frontend\                         ← React (Vite)
│   ├── package.json
│   ├── vite.config.js                (dev proxy → :8080)
│   └── src\
│       ├── api\axios.js              interceptors
│       ├── services\*.js             endpoint wrappers
│       ├── context\AuthContext.jsx, NotificationContext.jsx
│       ├── routes\PublicRoutes.jsx, ClientRoutes.jsx, AdminRoutes.jsx
│       ├── layouts\PublicLayout.jsx, DashboardLayout.jsx, AdminLayout.jsx
│       ├── pages\public\… auth\… client\… admin\…
│       ├── components\common\… charts\… forms\…
│       ├── hooks\ utils\ animations\ assets\ styles\ types\
│       ├── App.jsx  main.jsx
└── docker-compose.yml                (Phase 23: mysql + backend + nginx/frontend)
```

---

## 16. Development Roadmap (23 phases, gate-checked)

1. Foundation — repos, Maven/Vite setup, MySQL connect, health API, base layouts
2. Database — all entities/migrations/seed (roles, ports, categories)
3. Authentication — register/login/JWT/roles/reset/guards
4. Public website — all marketing pages + available shipments
5. Client dashboard shell — stats, requirement wizard (5 steps), enquiries, notifications, profile
6. Admin dashboard shell — masters: users, clients, vessels, cargo, categories, ports
7. Enquiry & negotiation — admin pipeline, append-only offer threads
8. Quotation — builder, PDF, email, secure link, accept/reject
9. Tax engine — tax_rates CRUD, treatment selection, BigDecimal calculator
10. Proforma invoice — generate/PDF/email/dashboard
11. Tax invoice — sequential INV, GST split display, PDF/email
12. Payments — Razorpay order/checkout/verify/webhook (sandbox-tested)
13. Receipts — REC PDF/email/history
14. Final bill — adjustments, balance, PAID state
15. Shipment management — creation, assignments, 11-state workflow
16. Tracking — manual entries, timeline, Leaflet map
17. Documents — Cloudinary uploads/downloads/permissions
18. Reviews — rating/moderation gated on completed shipments
19. Reports — revenue/outstanding/cargo/enquiry analytics
20. Audit & security hardening — audit viewer, lockouts, rate limits
21. UI polish — responsive, skeletons, empty/error states, motion pass
22. Testing — JUnit/Mockito backend, Vitest/RTL frontend, API smoke suite
23. Deployment — builds, docker-compose, env/secrets, domain + HTTPS checklist

Each phase ends with build + run verification and waits for your go-ahead.

---

## 17. Required Dependencies

Backend (`pom.xml`, Java 17, Boot 3.3.x):

```
spring-boot-starter-web, spring-boot-starter-security,
spring-boot-starter-data-jpa, spring-boot-starter-validation,
spring-boot-starter-mail, spring-boot-starter-aop, spring-boot-starter-test,
mysql-connector-j, flyway-core + flyway-mysql,
io.jsonwebtoken:jjwt-api/-impl/-jackson 0.12.x,
org.projectlombok:lombok, org.mapstruct:mapstruct + processor,
com.github.librepdf:openpdf, com.razorpay:razorpay-java,
com.cloudinary:cloudinary-http5, springdoc-openapi-starter-webmvc-ui (dev docs)
```

Frontend:

```
react, react-dom, react-router-dom, axios,
tailwindcss (+ postcss, autoprefixer), framer-motion, recharts,
leaflet, react-leaflet, react-hot-toast, date-fns
(dev) vite, @vitejs/plugin-react, eslint(+react hooks/refresh), vitest, @testing-library/react
```

Tooling required locally: JDK 17+, Maven 3.9+, Node 20+, MySQL 8.

---

## 18. Environment Variables

Backend (`backend/.env`, git-ignored; `application.yml` maps `${…}`):

```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/exportplatform?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# JWT
JWT_SECRET=<random-256-bit-base64>
JWT_EXPIRATION_MS=86400000

# Razorpay (test keys first)
RAZORPAY_KEY_ID=rzp_test_xxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxxxx

# Cloudinary
CLOUDINARY_CLOUD_NAME=xxxxx
CLOUDINARY_API_KEY=xxxxxxxxxx
CLOUDINARY_API_SECRET=xxxxxxxxxxxxxxx

# Mail (SMTP)
MAIL_HOST=smtp-relay.example.com
MAIL_PORT=587
MAIL_USERNAME=no-reply@example.com
MAIL_PASSWORD=xxxxxxxx

# App
CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_BASE_URL=http://localhost:5173
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

Frontend (`frontend/.env` — public values only):

```properties
VITE_API_BASE_URL=http://localhost:8080/api
VITE_RAZORPAY_KEY_ID=rzp_test_xxxxxxxx   # public key only
```

---

**Status:** Architecture complete — awaiting confirmation to begin **PHASE 1 — PROJECT SETUP** (backend scaffold + frontend scaffold + MySQL connection + health-check API + base layouts).
