COMPLETE MASTER PROMPT



GLOBAL EXPORT, SHIPPING, CARGO, QUOTATION, TAX INVOICE, PAYMENT \& BILLING PLATFORM



You are a senior full-stack software architect, Java Spring Boot developer, React developer, MySQL database architect, UI/UX designer, security engineer, payment integration developer, and DevOps engineer.



I want to build a premium, modern, secure, scalable, production-ready international export and shipping management platform.



This is a real business application, not a simple demo website.



\---



1\. MANDATORY TECHNOLOGY STACK



Use EXACTLY the following primary technology stack.



FRONTEND



Use:



\- React.js

\- JavaScript

\- React Router

\- Axios

\- Tailwind CSS

\- Framer Motion

\- Recharts

\- Leaflet or Mapbox



Do NOT use Angular.



Do NOT use Vue.



Do NOT replace React with another frontend framework.



\---



2\. BACKEND



Use:



\- Java

\- Spring Boot

\- Spring Web

\- Spring Security

\- JWT Authentication

\- Spring Data JPA

\- Hibernate

\- Bean Validation

\- Maven



Do NOT use:



\- Node.js

\- Express.js

\- NestJS

\- Sequelize



The backend must be a proper Java Spring Boot REST API.



\---



3\. DATABASE



Use:



MySQL



Use:



\- MySQL 8+

\- Spring Data JPA

\- Hibernate

\- Proper relational database design

\- Foreign keys

\- Indexes

\- Unique constraints

\- Transactions

\- Decimal types for financial amounts



Do not use MongoDB as the primary database.



\---



4\. FILE AND IMAGE STORAGE



Use a cloud storage service such as:



Cloudinary



for:



\- Company logo

\- Vessel images

\- Cargo images

\- User documents

\- Export documents

\- Certificates

\- Invoice PDFs

\- Quotation PDFs

\- Other files



Do not store large images directly inside MySQL.



Store secure URLs and metadata in MySQL.



\---



5\. PAYMENT



Design payment integration using a modular service architecture.



For the initial India deployment, support:



Razorpay



Payment integration must be implemented in the Java Spring Boot backend.



Never expose payment secrets to React.



Use environment variables or secure configuration.



Example:



RAZORPAY\_KEY\_ID



RAZORPAY\_KEY\_SECRET



\---



6\. EMAIL



Use Java/Spring-compatible email functionality.



Recommended:



\- Spring Boot Mail

\- SMTP

\- A transactional email provider if required



Send emails for:



\- Registration

\- Export enquiry

\- Quotation

\- Quotation acceptance

\- Negotiation

\- Proforma invoice

\- Invoice

\- Payment

\- Receipt

\- Shipment updates

\- Final bill

\- Contact enquiries



\---



7\. BUSINESS CONCEPT



Our company provides international export/shipping coordination services.



Example:



A client wants:



Cargo:



Iron Ore



Quantity:



60,000 MT



Origin:



India



Destination:



UAE



Loading Date:



15 September 2026



Budget:



Client's expected budget



The client submits the requirement.



Our admin team receives the enquiry.



Admin reviews available cargo and vessel options.



Admin communicates with the client.



Admin negotiates the price.



Admin creates a quotation.



Client accepts the quotation.



Then:



Quotation



↓



Proforma Invoice



↓



Advance Payment



↓



Shipment Confirmation



↓



Cargo Loading



↓



Ship Departure



↓



Shipment Tracking



↓



Final Tax Invoice



↓



Balance Payment



↓



Payment Receipt



↓



Final Bill



↓



Delivery



↓



Review



\---



8\. COMPLETE SYSTEM FLOW



The platform must support:



CLIENT



↓



REGISTER



↓



LOGIN



↓



EXPORT REQUIREMENT



↓



ENQUIRY



↓



ADMIN REVIEW



↓



NEGOTIATION



↓



QUOTATION



↓



CLIENT ACCEPTANCE



↓



PROFORMA INVOICE



↓



ADVANCE PAYMENT



↓



SHIPMENT



↓



TRACKING



↓



FINAL INVOICE



↓



BALANCE PAYMENT



↓



RECEIPT



↓



FINAL BILL



↓



DELIVERY



↓



REVIEW



\---



9\. PUBLIC WEBSITE



Create a premium international export website.



Pages:



\- Home

\- About Us

\- Services

\- Export Solutions

\- Cargo Categories

\- Available Shipments

\- How It Works

\- Shipment Tracking

\- Contact Us

\- Careers

\- Login

\- Register



\---



10\. PREMIUM HOME PAGE



The website must look like a professional international shipping/export company.



It must NOT look like a basic Bootstrap template.



Use:



\- Professional maritime images

\- Cargo ship visuals

\- Port visuals

\- Ocean backgrounds

\- Smooth animations

\- Scroll animations

\- Premium typography

\- Modern cards

\- Interactive sections

\- Animated statistics

\- Professional icons

\- Responsive design



Hero section:



"Connecting Global Trade Through Reliable Export Solutions"



Buttons:



"Request Export Quote"



"Explore Shipments"



Sections:



1\. Hero

2\. About Us

3\. Export Services

4\. Cargo Categories

5\. Available Shipments

6\. Global Network

7\. How It Works

8\. Shipment Tracking

9\. Why Choose Us

10\. Statistics

11\. Testimonials

12\. Call To Action

13\. Footer



\---



11\. AVAILABLE SHIPMENTS



Public users can see non-sensitive shipment information.



Show:



\- Shipment ID

\- Cargo

\- Category

\- Quantity

\- Origin

\- Destination

\- Loading Date

\- Estimated Arrival

\- Indicative Price / MT

\- Status

\- General vessel information

\- Image



Example:



60,000 MT



Iron Ore



India → UAE



Loading:



15 September 2026



Indicative Price:



₹XXXX / MT



Status:



Available



Button:



"Request Quote"



Do NOT publicly expose:



\- Private vessel management phone numbers

\- Private management information

\- Internal financial information

\- Sensitive operational information



\---



12\. CARGO CATEGORIES



Create dynamic cargo categories.



Examples:



\- Iron Ore

\- Coal

\- Steel

\- Cement

\- Rice

\- Wheat

\- Agricultural Products

\- Minerals

\- Chemicals

\- Machinery

\- General Cargo

\- Bulk Cargo

\- Container Cargo



Admin can:



\- Create

\- Edit

\- Delete

\- Activate

\- Deactivate



Do not hard-code categories.



\---



13\. USER REGISTRATION



Fields:



\- Full Name

\- Company Name

\- Email

\- Phone

\- Country

\- Password



Implement:



\- Register

\- Login

\- Logout

\- Forgot Password

\- Reset Password

\- JWT authentication

\- Password hashing

\- Protected routes

\- Role-based authorization



Use Spring Security.



Use BCryptPasswordEncoder.



\---



14\. USER ROLES



Initial roles:



\- CLIENT

\- ADMIN

\- SHIP\_MANAGER



Future roles:



\- SUPER\_ADMIN

\- SALES

\- OPERATIONS

\- FINANCE

\- ACCOUNTANT



Use Spring Security role-based authorization.



\---



15\. CLIENT DASHBOARD



After login:



Display:



\- Total enquiries

\- Active shipments

\- Pending quotations

\- Active negotiations

\- Pending payments

\- Outstanding amount

\- Completed shipments



Pages:



\- Dashboard

\- Export Requirements

\- My Enquiries

\- My Quotations

\- My Negotiations

\- My Proforma Invoices

\- My Invoices

\- My Payments

\- My Receipts

\- My Shipments

\- Shipment Tracking

\- Documents

\- Notifications

\- Reviews

\- Profile

\- Logout



\---



16\. EXPORT REQUIREMENT FORM



Create a professional multi-step React form.



Step 1 — Client Information



\- Name

\- Company

\- Email

\- Phone



Step 2 — Cargo



\- Cargo Type

\- Cargo Category

\- Quantity

\- Unit

\- Description



Step 3 — Shipping



\- Origin Country

\- Origin Location

\- Loading Port

\- Destination Country

\- Destination Location

\- Destination Port

\- Required Loading Date

\- Expected Delivery Date



Step 4 — Budget



\- Currency

\- Estimated Budget

\- Target Price / MT



Step 5 — Additional Requirements



\- Message

\- Attachments

\- Documents



Submit:



"Submit Export Requirement"



Backend must validate the data using Spring Validation.



After submission:



\- Save enquiry

\- Notify admin

\- Send email

\- Show confirmation



\---



17\. ADMIN DASHBOARD



Create a separate premium Admin Dashboard.



Sections:



\- Dashboard

\- Users

\- Clients

\- Vessels

\- Cargo

\- Cargo Categories

\- Ports

\- Enquiries

\- Quotations

\- Negotiations

\- Proforma Invoices

\- Tax

\- Invoices

\- Payments

\- Receipts

\- Shipments

\- Shipment Tracking

\- Documents

\- Reviews

\- Notifications

\- Reports

\- Audit Logs

\- Settings



\---



18\. ADMIN STATISTICS



Display:



\- Total Clients

\- Total Enquiries

\- Total Quotations

\- Active Shipments

\- Completed Shipments

\- Total Cargo Volume

\- Total Invoiced

\- Total Paid

\- Total Outstanding

\- Pending Payments

\- Revenue



Charts:



\- Monthly enquiries

\- Monthly revenue

\- Cargo volume

\- Shipment status

\- Payment status

\- Outstanding invoices



Use Recharts.



\---



19\. VESSEL MANAGEMENT



Admin can create, update, view, and manage vessels.



Fields:



\- Vessel Name

\- IMO Number

\- Vessel Type

\- Capacity

\- Flag

\- Current Location

\- Status

\- Management Company

\- Management Contact

\- Description

\- Images



Statuses:



\- Available

\- Loading

\- Loading Completed

\- In Transit

\- Arrived

\- Maintenance

\- Unavailable



Sensitive management information must only be accessible to authorized roles.



\---



20\. CARGO MANAGEMENT



Fields:



\- Cargo Name

\- Cargo Category

\- Description

\- Quantity

\- Unit

\- Origin

\- Destination

\- Loading Port

\- Destination Port

\- Loading Date

\- Estimated Arrival

\- Indicative Price / MT

\- Currency

\- Status

\- Images

\- Documents



Statuses:



\- Available

\- Reserved

\- Loading

\- In Transit

\- Delivered

\- Cancelled



\---



21\. PORT MANAGEMENT



Admin can manage:



\- Port Name

\- Country

\- City

\- Port Code

\- Latitude

\- Longitude

\- Status



Ports must be reusable across:



\- Cargo

\- Shipments

\- Quotations

\- Invoices

\- Tracking



\---



22\. ENQUIRY MANAGEMENT



Admin sees:



\- Enquiry ID

\- Client

\- Company

\- Phone

\- Email

\- Cargo

\- Quantity

\- Origin

\- Destination

\- Loading Date

\- Budget

\- Status

\- Created Date



Statuses:



\- New

\- Reviewing

\- Contacted

\- Negotiating

\- Quotation Sent

\- Approved

\- Rejected

\- Converted

\- Closed



\---



23\. NEGOTIATION SYSTEM



Support price negotiation.



Example:



Client:



60,000 MT



Initial Price:



₹4,500 / MT



Admin Offer:



₹4,350 / MT



Client Counter Offer:



₹4,200 / MT



Final:



₹4,275 / MT



Every negotiation action must be stored.



Never overwrite historical offers.



Each negotiation record contains:



\- Sender

\- Offer Price

\- Message

\- Date/time

\- Status



\---



24\. QUOTATION SYSTEM



Admin creates quotation from an enquiry.



Fields:



\- Quotation Number

\- Date

\- Valid Until

\- Client

\- Company

\- Billing Address

\- Shipping Address

\- Email

\- Phone

\- GSTIN

\- Country

\- Cargo

\- Quantity

\- Unit

\- Rate / MT

\- Freight Charges

\- Loading Charges

\- Documentation Charges

\- Insurance

\- Other Charges

\- Discount

\- Tax

\- Subtotal

\- Grand Total

\- Currency

\- Payment Terms

\- Delivery Terms

\- Incoterms

\- Origin

\- Destination

\- Loading Date

\- Delivery Date

\- Notes

\- Terms and Conditions



Example:



QUO-2026-000001



Quotation numbers must be generated on the backend.



\---



25\. QUOTATION STATUS



Use:



\- Draft

\- Sent

\- Viewed

\- Negotiating

\- Accepted

\- Rejected

\- Expired

\- Converted

\- Cancelled



Do not delete historical commercial documents unnecessarily.



\---



26\. SEND QUOTATION



Admin clicks:



"Send Quotation"



Backend:



1\. Generate quotation.

2\. Generate PDF.

3\. Store document reference.

4\. Send email.

5\. Update status.

6\. Create audit record.



Email contains:



\- Quotation number

\- Amount

\- Validity

\- PDF attachment

\- Secure View button



\---



27\. CLIENT QUOTATION VIEW



Create secure page:



/quotation/:secureToken



Client can:



\- View quotation

\- Download PDF

\- Accept quotation

\- Reject quotation

\- Request changes



Do not expose internal database IDs unnecessarily.



\---



28\. QUOTATION ACCEPTANCE



When client accepts:



\- Save acceptance timestamp

\- Save client ID

\- Update quotation status

\- Notify admin

\- Create audit record

\- Enable Proforma Invoice generation



\---



29\. TAX SYSTEM



Create configurable tax management.



Do NOT hard-code GST rates.



Admin can configure:



\- Tax Name

\- Tax Type

\- Rate

\- Country

\- Jurisdiction

\- Effective Date

\- Active/Inactive



Support configurable:



\- CGST

\- SGST

\- IGST

\- GST Exempt

\- Zero-rated

\- Export-related treatment



Tax treatment must be selected according to the transaction.



Do not assume every export transaction has the same tax treatment.



\---



30\. TAX CALCULATION



Backend calculates:



Subtotal



minus Discount



plus Charges



equals Taxable Amount



Then:



Taxable Amount



plus Applicable Tax



equals Grand Total



All financial calculations must happen in the Java backend.



Never trust frontend totals.



Use Java BigDecimal for financial calculations.



\---



31\. PROFORMA INVOICE



Create Proforma Invoice.



Fields:



\- PI Number

\- Date

\- Client

\- Cargo

\- Quantity

\- Rate

\- Charges

\- Tax Treatment

\- Total

\- Payment Terms

\- Bank Details

\- Company Details



Example:



PI-2026-000001



Generate professional PDF.



Send to client.



\---



32\. TAX INVOICE



Create tax invoice.



Fields:



\- Invoice Number

\- Invoice Date

\- Due Date

\- Client

\- Billing Address

\- Shipping Address

\- GSTIN

\- PAN where applicable

\- Cargo

\- Quantity

\- Rate

\- Taxable Value

\- Discount

\- CGST

\- SGST

\- IGST

\- Other Applicable Tax

\- Total Tax

\- Grand Total

\- Currency

\- Payment Terms

\- Bank Details

\- Company GSTIN

\- Company PAN

\- Company Address

\- Company Logo

\- Authorized Signature



Example:



INV-2026-000001



\---



33\. EXPORT INVOICE SUPPORT



Support:



\- Export customer

\- Country

\- Currency

\- Port of Loading

\- Port of Discharge

\- Destination

\- Incoterms

\- Export reference

\- Shipping information

\- Foreign currency

\- Exchange rate

\- INR equivalent where applicable

\- Export-related tax treatment



Tax and export treatment must remain configurable.



\---



34\. PAYMENT SYSTEM



Support:



\- Advance Payment

\- Partial Payment

\- Full Payment

\- Balance Payment

\- Refund



Example:



Invoice:



₹10,00,000



Advance:



₹3,00,000



Balance:



₹7,00,000



Backend automatically calculates:



\- Total

\- Paid

\- Balance

\- Status



\---



35\. PAYMENT STATUS



Use:



\- Pending

\- Processing

\- Paid

\- Partially Paid

\- Failed

\- Cancelled

\- Refunded

\- Partially Refunded



\---



36\. RAZORPAY INTEGRATION



Implement Razorpay integration in Spring Boot.



Create:



POST /api/payments/create-order



The backend creates the payment order.



React opens the Razorpay checkout.



After payment:



POST /api/payments/verify



Also implement:



POST /api/payments/webhook



The backend must verify Razorpay signatures.



Never trust frontend-only payment confirmation.



\---



37\. PAYMENT WEBHOOK



Webhook must:



1\. Verify signature.

2\. Validate event.

3\. Find transaction.

4\. Prevent duplicate processing.

5\. Update payment.

6\. Update invoice.

7\. Calculate balance.

8\. Generate receipt.

9\. Send email.

10\. Notify admin.



Webhook processing must be idempotent.



Use proper database transactions.



\---



38\. PAYMENT RECEIPT



After successful payment generate:



REC-2026-000001



Include:



\- Receipt Number

\- Date

\- Client

\- Invoice Number

\- Amount

\- Payment Method

\- Transaction ID

\- Remaining Balance

\- Company Information



Generate PDF.



Send receipt by email.



\---



39\. FINAL BILL



Final bill must contain:



\- Final quantity

\- Final rate

\- Additional charges

\- Discount

\- Applicable tax

\- Advance payment

\- Previous payments

\- Balance

\- Final payable amount



Example:



Final Invoice:



₹50,00,000



Advance:



₹15,00,000



Balance:



₹35,00,000



After balance payment:



PAID



\---



40\. OFFLINE PAYMENT



Authorized admins can record:



\- Bank Transfer

\- NEFT

\- RTGS

\- IMPS

\- Cheque

\- Other approved methods



Fields:



\- Amount

\- Date

\- Method

\- Transaction Reference

\- Notes

\- Proof Document



All manual payments must be audited.



\---



41\. BILLING DASHBOARD



Admin:



\- Total Invoiced

\- Total Paid

\- Total Outstanding

\- Total Overdue

\- Advance Payments

\- Pending Payments

\- Refunds



Charts:



\- Monthly Revenue

\- Payment Collection

\- Outstanding

\- Invoice Status



\---



42\. CLIENT BILLING DASHBOARD



Client sees:



\- Quotations

\- Accepted Quotations

\- Proforma Invoices

\- Tax Invoices

\- Payments

\- Receipts

\- Outstanding Balance

\- Payment History



Buttons:



\- View

\- Download PDF

\- Pay Now



\---



43\. PDF DOCUMENT GENERATION



Generate professional PDFs for:



\- Quotations

\- Proforma Invoices

\- Tax Invoices

\- Final Invoices

\- Payment Receipts



Each document should contain:



\- Company logo

\- Company name

\- Address

\- Contact

\- GSTIN

\- PAN where applicable

\- Document number

\- Date

\- Client details

\- Cargo

\- Quantity

\- Rate

\- Charges

\- Discount

\- Tax

\- Total

\- Payment Terms

\- Bank Details

\- Terms \& Conditions

\- Authorized Signature



Use a professional corporate layout.



\---



44\. DOCUMENT NUMBERING



Use backend-controlled sequential numbering.



Examples:



QUO-2026-000001



PI-2026-000001



INV-2026-000001



REC-2026-000001



Prevent duplicate numbers using database transactions/locking where necessary.



\---



45\. SHIPMENT MANAGEMENT



Shipment fields:



\- Shipment ID

\- Client

\- Vessel

\- Cargo

\- Quantity

\- Origin

\- Destination

\- Loading Port

\- Destination Port

\- Loading Date

\- Estimated Arrival

\- Final Price

\- Currency

\- Shipment Status



Statuses:



1\. Booking Confirmed

2\. Cargo Preparation

3\. Loading

4\. Loading Completed

5\. Departed

6\. In Transit

7\. Near Destination

8\. Arrived

9\. Unloading

10\. Delivered

11\. Completed



\---



46\. MANUAL SHIPMENT TRACKING



Initially use manual tracking.



Admin enters:



\- Latitude

\- Longitude

\- Current Location

\- Status

\- Date/time

\- Tracking Notes



Client sees:



\- Current Location

\- Status

\- Timeline

\- Map

\- Last Updated



The architecture must allow future AIS/live vessel tracking API integration.



\---



47\. TRACKING PAGE



Client enters Shipment ID.



Show:



\- Shipment

\- Vessel

\- Cargo

\- Quantity

\- Origin

\- Destination

\- Current Location

\- Loading Date

\- ETA

\- Timeline

\- Map



Timeline:



Booking Confirmed



↓



Loading



↓



Departed



↓



In Transit



↓



Arrived



↓



Delivered



\---



48\. DOCUMENT MANAGEMENT



Support:



\- Invoice

\- Packing List

\- Bill of Lading

\- Customs Documents

\- Export Documents

\- Certificates

\- Contracts

\- Other Documents



Use Cloudinary or appropriate secure cloud storage.



Restrict documents by role and ownership.



\---



49\. REVIEW SYSTEM



After completed shipment:



Client can submit:



\- Rating

\- Review

\- Feedback



Admin can:



\- Approve

\- Hide

\- Delete



Only completed shipments can receive reviews.



\---



50\. NOTIFICATION SYSTEM



Create in-app notifications.



Examples:



"Your enquiry has been received."



"New quotation received."



"Quotation accepted."



"New negotiation offer."



"Payment successful."



"Invoice generated."



"Shipment departed."



"Shipment location updated."



"Shipment arrived."



"Final bill generated."



Allow:



\- Read

\- Unread

\- Mark all as read



\---



51\. DATABASE ENTITIES



Create appropriate JPA entities/tables.



Minimum:



users



roles



clients



vessels



vessel\_images



cargo\_categories



cargo



ports



enquiries



negotiations



negotiation\_messages



quotations



quotation\_items



tax\_rates



proforma\_invoices



proforma\_invoice\_items



invoices



invoice\_items



payments



payment\_transactions



payment\_webhooks



receipts



refunds



shipments



shipment\_tracking



documents



notifications



reviews



contact\_messages



billing\_addresses



shipping\_addresses



document\_sequences



audit\_logs



password\_resets



\---



52\. DATABASE RELATIONSHIPS



Example:



User



↓



Enquiry



↓



Quotation



↓



Quotation Items



↓



Accepted Quotation



↓



Proforma Invoice



↓



Payment



↓



Shipment



↓



Final Invoice



↓



Balance Payment



↓



Receipt



\---



53\. SPRING BOOT PACKAGE STRUCTURE



Use a clean package structure:



src/main/java/com/company/exportplatform/



config/



controller/



dto/



entity/



repository/



service/



service/impl/



security/



exception/



validation/



mapper/



specification/



payment/



email/



pdf/



cloudinary/



audit/



notification/



util/



ExportPlatformApplication.java



Do not put all backend code inside controllers.



\---



54\. FRONTEND STRUCTURE



Use:



src/



assets/



components/



pages/



&#x20;   public/



&#x20;   auth/



&#x20;   client/



&#x20;   admin/



layouts/



routes/



services/



hooks/



context/



utils/



animations/



api/



types/



styles/



App.jsx



main.jsx



\---



55\. REST API STRUCTURE



Authentication:



POST /api/auth/register



POST /api/auth/login



POST /api/auth/forgot-password



POST /api/auth/reset-password



Users:



GET /api/users



GET /api/users/:id



Vessels:



GET /api/vessels



POST /api/vessels



PUT /api/vessels/:id



DELETE /api/vessels/:id



Cargo:



GET /api/cargo



POST /api/cargo



PUT /api/cargo/:id



DELETE /api/cargo/:id



Enquiries:



POST /api/enquiries



GET /api/enquiries



GET /api/enquiries/:id



PUT /api/enquiries/:id



Quotations:



POST /api/quotations



GET /api/quotations



GET /api/quotations/:id



PUT /api/quotations/:id



POST /api/quotations/:id/send



POST /api/quotations/:id/accept



POST /api/quotations/:id/reject



Proforma:



POST /api/proforma-invoices



GET /api/proforma-invoices



GET /api/proforma-invoices/:id



Invoices:



POST /api/invoices



GET /api/invoices



GET /api/invoices/:id



POST /api/invoices/:id/send



Payments:



POST /api/payments/create-order



POST /api/payments/verify



POST /api/payments/webhook



GET /api/payments



GET /api/payments/:id



POST /api/refunds



Receipts:



GET /api/receipts



GET /api/receipts/:id



Shipments:



GET /api/shipments



POST /api/shipments



GET /api/shipments/:id



PUT /api/shipments/:id



Tracking:



GET /api/shipments/:id/tracking



POST /api/shipments/:id/tracking



Reviews:



POST /api/reviews



GET /api/reviews



Notifications:



GET /api/notifications



PUT /api/notifications/:id/read



Billing:



GET /api/billing/dashboard



\---



56\. API SECURITY



Use Spring Security.



Protect:



\- Admin APIs

\- Client APIs

\- Financial APIs

\- Payment APIs

\- Documents

\- Shipments



Public APIs should only expose intentionally public information.



Use:



\- JWT

\- Role-based authorization

\- Method-level security where appropriate

\- DTOs

\- Bean Validation

\- Global exception handling

\- CORS configuration

\- Rate limiting where appropriate



\---



57\. DTO RULE



Do not expose JPA entities directly from REST APIs.



Use DTOs for:



\- Requests

\- Responses

\- Authentication

\- Quotations

\- Invoices

\- Payments

\- Shipments

\- Users



\---



58\. ERROR HANDLING



Implement global Spring Boot exception handling using:



@RestControllerAdvice



Provide consistent API responses.



Example:



{

"success": false,

"message": "Quotation not found",

"timestamp": "...",

"status": 404

}



Do not expose internal stack traces to clients.



\---



59\. FINANCIAL CALCULATION



Use:



Java BigDecimal



for:



\- Quantity where appropriate

\- Rate

\- Discount

\- Tax

\- Subtotal

\- Total

\- Paid Amount

\- Balance



Never rely on JavaScript calculations as the source of truth.



The backend must recalculate all financial values.



\---



60\. AUDIT LOG



Track:



\- User created

\- Quotation created

\- Quotation modified

\- Quotation accepted

\- Invoice created

\- Invoice modified

\- Payment created

\- Payment verified

\- Refund created

\- Shipment updated

\- Tax changed

\- Manual payment added

\- Document generated



Store:



\- User

\- Action

\- Entity

\- Entity ID

\- Old Value

\- New Value

\- Timestamp

\- IP where appropriate



Never silently overwrite financial history.



\---



61\. SEARCH AND FILTERING



Users:



\- Cargo

\- Category

\- Origin

\- Destination

\- Quantity

\- Date



Admin:



\- Client

\- Cargo

\- Vessel

\- Shipment

\- Enquiry

\- Quotation

\- Invoice

\- Payment

\- Status

\- Date



Support:



\- Search

\- Filtering

\- Sorting

\- Pagination



Use backend pagination for large datasets.



\---



62\. RESPONSIVE DESIGN



Support:



\- Desktop

\- Laptop

\- Tablet

\- Mobile



The admin dashboard and client dashboard must both be responsive.



\---



63\. PREMIUM UI/UX



Use:



\- Tailwind CSS

\- Framer Motion

\- Smooth transitions

\- Scroll animations

\- Hover effects

\- Micro-interactions

\- Skeleton loaders

\- Toast notifications

\- Loading states

\- Error states

\- Empty states

\- Confirmation dialogs



Do not overuse animations.



The website must look professional and trustworthy.



\---



64\. PAYMENT SECURITY



Never:



\- Store card details

\- Store CVV

\- Store banking passwords

\- Expose Razorpay secret

\- Trust frontend payment success



Always:



\- Verify payment server-side

\- Verify webhook signature

\- Use HTTPS in production

\- Use idempotent payment processing

\- Store gateway transaction IDs

\- Record audit logs

\- Use database transactions



\---



65\. TAX/LEGAL SAFETY



Tax logic must be configurable.



Do not assume a single GST rate or export tax treatment.



The system should allow the authorized finance/admin team to select the applicable tax treatment.



For actual production invoicing, the business should validate the final tax configuration and invoice format with a qualified Indian tax professional/CA.



\---



66\. FUTURE FEATURES



Architecture should support:



\- AIS vessel tracking

\- Live vessel location

\- WhatsApp notifications

\- SMS

\- Multi-language

\- Multi-currency

\- Digital contracts

\- E-signature

\- Accounting integration

\- ERP integration

\- Mobile application

\- Advanced analytics

\- Automated pricing



Do not implement these until requested.



\---



67\. DEVELOPMENT PHASES



Do NOT generate the entire project in one response.



Build the project phase-by-phase.



PHASE 1 — FOUNDATION



\- Project architecture

\- React setup

\- Spring Boot setup

\- Maven

\- MySQL connection

\- Environment configuration

\- Basic REST API

\- Frontend/backend connection

\- Base layouts



PHASE 2 — DATABASE



\- JPA entities

\- Relationships

\- Repositories

\- Database migrations/schema

\- Seed data



PHASE 3 — AUTHENTICATION



\- Register

\- Login

\- JWT

\- Spring Security

\- Roles

\- Forgot password

\- Reset password

\- Protected React routes



PHASE 4 — PUBLIC WEBSITE



\- Home

\- About

\- Services

\- Cargo

\- Shipments

\- Contact

\- Login

\- Register



PHASE 5 — CLIENT DASHBOARD



\- Dashboard

\- Export requirement

\- Enquiries

\- Profile

\- Notifications



PHASE 6 — ADMIN DASHBOARD



\- Users

\- Clients

\- Vessels

\- Cargo

\- Categories

\- Ports



PHASE 7 — ENQUIRY \& NEGOTIATION



\- Enquiry management

\- Negotiation

\- Offers

\- Counter offers

\- History



PHASE 8 — QUOTATION



\- Create quotation

\- Edit quotation

\- PDF

\- Email

\- Client view

\- Accept/reject



PHASE 9 — TAX



\- Tax configuration

\- CGST

\- SGST

\- IGST

\- Export/zero-rated configuration

\- Tax calculation



PHASE 10 — PROFORMA INVOICE



\- Generate

\- PDF

\- Email

\- Client dashboard



PHASE 11 — TAX INVOICE



\- Generate invoice

\- Sequential numbering

\- PDF

\- Email

\- Tax calculation



PHASE 12 — PAYMENT



\- Razorpay

\- Order creation

\- Checkout

\- Verification

\- Webhook

\- Payment status



PHASE 13 — RECEIPTS



\- Payment receipt

\- PDF

\- Email

\- Payment history



PHASE 14 — FINAL BILL



\- Final quantity

\- Additional charges

\- Advance adjustment

\- Balance

\- Final invoice

\- Final bill



PHASE 15 — SHIPMENT



\- Shipment creation

\- Vessel assignment

\- Cargo assignment

\- Status



PHASE 16 — TRACKING



\- Manual tracking

\- Map

\- Coordinates

\- Timeline



PHASE 17 — DOCUMENTS



\- Cloudinary

\- Upload

\- Download

\- Permissions



PHASE 18 — REVIEWS



\- Ratings

\- Reviews

\- Admin moderation



PHASE 19 — REPORTS



\- Revenue

\- Payments

\- Outstanding

\- Cargo

\- Shipments

\- Enquiries

\- Clients



PHASE 20 — AUDIT \& SECURITY



\- Audit logs

\- Security

\- Validation

\- Authorization

\- Payment security



PHASE 21 — UI POLISH



\- Animations

\- Responsive design

\- Mobile optimization

\- Loading states

\- Error states



PHASE 22 — TESTING



\- Frontend tests

\- Backend tests

\- API tests

\- Database tests

\- Payment tests

\- Security tests



PHASE 23 — DEPLOYMENT



\- React production build

\- Spring Boot deployment

\- MySQL production

\- Cloudinary

\- Environment variables

\- Domain

\- HTTPS

\- Production security



\---



68\. CODING RULE



Whenever I ask for code:



1\. Give the exact file path.

2\. Give the complete file.

3\. Do not give incomplete code unless requested.

4\. Do not remove existing functionality.

5\. Keep frontend and backend fields synchronized.

6\. Keep API contracts synchronized.

7\. Explain where to place the file.

8\. Give required Maven/npm commands.

9\. Give database migration instructions if needed.

10\. Explain expected result.



\---



69\. ERROR FIXING RULE



If I provide an error:



First identify:



\- Exact file

\- Exact line

\- Root cause

\- Dependency issue

\- API issue

\- Database issue

\- Configuration issue



Then provide:



1\. Root cause

2\. Solution

3\. Complete corrected file

4\. Required dependency

5\. Required database change

6\. Command to run

7\. Expected result



Do not randomly modify unrelated files.



\---



70\. FRONTEND/BACKEND CONSISTENCY



React and Spring Boot must use exactly matching:



\- Field names

\- Data types

\- API endpoints

\- Request bodies

\- Response bodies

\- Authentication

\- Error formats



Do not create frontend fields that do not exist in the backend.



Do not create backend fields that the frontend does not understand.



\---



71\. PROJECT QUALITY



The final project must be:



\- Production-ready

\- Secure

\- Responsive

\- Maintainable

\- Scalable

\- Modular

\- Well documented

\- Properly validated

\- Properly tested



Do not create a fake/demo-only architecture.



\---



72\. FINAL BUSINESS WORKFLOW



The final application must support:



CLIENT



↓



REGISTER



↓



LOGIN



↓



CREATE EXPORT REQUIREMENT



↓



ENQUIRY



↓



ADMIN REVIEW



↓



NEGOTIATION



↓



QUOTATION



↓



CLIENT RECEIVES QUOTATION



↓



CLIENT ACCEPTS



↓



PROFORMA INVOICE



↓



ADVANCE PAYMENT



↓



PAYMENT VERIFICATION



↓



SHIPMENT CONFIRMATION



↓



CARGO PREPARATION



↓



LOADING



↓



SHIP DEPARTURE



↓



SHIPMENT TRACKING



↓



ARRIVAL



↓



FINAL TAX INVOICE



↓



BALANCE PAYMENT



↓



PAYMENT RECEIPT



↓



FINAL BILL



↓



DELIVERY



↓



CLIENT REVIEW



\---



73\. MOST IMPORTANT RULES



This is a real business application.



Do not create fake payment success.



Do not fake payment verification.



Do not hard-code tax rates.



Do not hard-code financial calculations.



Do not expose sensitive admin information.



Do not trust frontend financial calculations.



Do not store payment card information.



Do not delete financial history.



Do not expose JPA entities directly.



Use DTOs.



Use Java BigDecimal for money.



Use Spring Security.



Use JWT.



Use database transactions.



Use audit logs.



Use secure payment webhooks.



Use proper MySQL relationships.



\---



74\. START THE PROJECT



Before writing implementation code, provide:



1\. Final system architecture

2\. Frontend architecture

3\. Spring Boot backend architecture

4\. MySQL ER relationship design

5\. Complete database table/entity list

6\. API architecture

7\. Authentication architecture

8\. Payment architecture

9\. Tax architecture

10\. Quotation-to-invoice workflow

11\. Shipment tracking architecture

12\. Cloudinary architecture

13\. Email architecture

14\. Security architecture

15\. Complete project folder structure

16\. Development roadmap

17\. Required dependencies

18\. Environment variables



Then STOP and wait for my confirmation.



After I confirm, start:



PHASE 1 — PROJECT SETUP



Build only Phase 1 first.



Do not jump directly to payment, invoice, or advanced features.



After each phase is completed and tested, wait for my confirmation before moving to the next phase.



Maintain complete consistency between React frontend, Java Spring Boot backend, and MySQL database throughout the entire project.



The final application must be a premium Global Export, Cargo, Vessel, Shipment, Quotation, Tax Invoice, Payment, Billing and Tracking Management Platform built with React.js + Java Spring Boot + MySQL.

