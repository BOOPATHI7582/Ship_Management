package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.DocumentResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Document;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.Invoice;
import com.company.exportplatform.entity.ProformaInvoice;
import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.Receipt;
import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.DocumentCategory;
import com.company.exportplatform.entity.enums.DocumentOwnerType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.DocumentRepository;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.InvoiceRepository;
import com.company.exportplatform.repository.ProformaInvoiceRepository;
import com.company.exportplatform.repository.QuotationRepository;
import com.company.exportplatform.repository.ReceiptRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.UserRepository;
import com.company.exportplatform.service.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Compliance documents: metadata in MySQL, bytes in the configured
 * FileStorage backend. Clients may only see/attach files for records they own.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "jpg", "jpeg", "png", "webp", "doc", "docx", "xls", "xlsx");

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EnquiryRepository enquiryRepository;
    private final QuotationRepository quotationRepository;
    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReceiptRepository receiptRepository;
    private final ShipmentRepository shipmentRepository;
    private final FileStorage fileStorage;
    private final AuditService auditService;

    // ---------- staff ----------

    @Transactional(readOnly = true)
    public List<DocumentResponse> listForStaff(DocumentOwnerType ownerType, Long ownerId) {
        return documentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public DocumentResponse uploadForStaff(String staffEmail, MultipartFile file,
                                           DocumentOwnerType ownerType, Long ownerId,
                                           String categoryRaw, String title) {
        User staff = requireUser(staffEmail);
        requireOwner(ownerType, ownerId);
        return save(file, ownerType, ownerId, categoryRaw, title, staff);
    }

    @Transactional(readOnly = true)
    public StoredDownload downloadForStaff(Long id) {
        return download(documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found")));
    }

    @Transactional
    public void deleteForStaff(Long id, String staffEmail) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        documentRepository.delete(document);
        try {
            fileStorage.delete(document.getPublicId());
        } catch (RuntimeException ex) {
            log.warn("Metadata deleted but storage cleanup failed for {}", document.getPublicId(), ex);
        }
        log.info("Document {} ({}) deleted by {}", id, document.getTitle(), staffEmail);
        auditService.record(staffEmail, "DOCUMENT_DELETED", "DOCUMENT", id,
                null, java.util.Map.of("title", document.getTitle(),
                        "ownerType", String.valueOf(document.getOwnerType()),
                        "ownerId", String.valueOf(document.getOwnerId())));
    }

    // ---------- client ----------

    @Transactional(readOnly = true)
    public List<DocumentResponse> listMine(String email, DocumentOwnerType ownerType, Long ownerId) {
        if (ownerType != null && ownerId != null) {
            requireOwnership(email, ownerType, ownerId);
            return documentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId)
                    .stream().map(this::toResponse).toList();
        }
        return allOwnedDocuments(email).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DocumentResponse uploadMine(String email, MultipartFile file,
                                       DocumentOwnerType ownerType, Long ownerId,
                                       String categoryRaw, String title) {
        User user = requireUser(email);
        Set<DocumentOwnerType> clientAttachable = Set.of(DocumentOwnerType.ENQUIRY, DocumentOwnerType.SHIPMENT,
                DocumentOwnerType.INVOICE, DocumentOwnerType.OTHER);
        if (!clientAttachable.contains(ownerType)) {
            throw new BadRequestException("Clients can attach documents to enquiries, shipments or invoices only");
        }
        requireOwnership(email, ownerType, ownerId);
        return save(file, ownerType, ownerId, categoryRaw, title, user);
    }

    @Transactional(readOnly = true)
    public StoredDownload downloadMine(String email, Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        requireOwnership(email, document.getOwnerType(), document.getOwnerId());
        return download(document);
    }

    // ---------- internals ----------

    private DocumentResponse save(MultipartFile file, DocumentOwnerType ownerType, Long ownerId,
                                  String categoryRaw, String title, User uploader) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("File exceeds the 10 MB limit");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException(
                    "Unsupported file type ." + ext + " (allowed: " + String.join(", ", ALLOWED_EXTENSIONS) + ")");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read uploaded file", ex);
        }
        String publicId = fileStorage.store(bytes, file.getOriginalFilename(), file.getContentType());

        DocumentCategory category = parseCategory(categoryRaw);
        Document document = new Document();
        document.setOwnerType(ownerType);
        document.setOwnerId(ownerId);
        document.setCategory(category);
        document.setTitle(firstNonBlank(title, file.getOriginalFilename(), category.name()));
        document.setPublicId(publicId);
        document.setSecureUrl("/api/documents/" + publicId + "/raw");
        document.setFileFormat(ext);
        document.setFileSizeBytes((long) bytes.length);
        document.setUploadedBy(uploader);

        Document saved = documentRepository.save(document);
        log.info("Document {} attached to {}/{} by {}",
                saved.getId(), ownerType, ownerId, uploader.getEmail());
        return toResponse(saved);
    }

    private StoredDownload download(Document document) {
        byte[] bytes = fileStorage.retrieve(document.getPublicId());
        String filename = (document.getTitle() != null ? document.getTitle() : "document");
        if (!filename.toLowerCase(Locale.ROOT).endsWith("." + document.getFileFormat())) {
            filename = filename + "." + document.getFileFormat();
        }
        return new StoredDownload(filename, bytes);
    }

    private void requireOwner(DocumentOwnerType ownerType, Long ownerId) {
        boolean exists = switch (ownerType) {
            case USER -> userRepository.existsById(ownerId);
            case ENQUIRY -> enquiryRepository.existsById(ownerId);
            case QUOTATION -> quotationRepository.existsById(ownerId);
            case PROFORMA_INVOICE -> proformaInvoiceRepository.existsById(ownerId);
            case INVOICE -> invoiceRepository.existsById(ownerId);
            case RECEIPT -> receiptRepository.existsById(ownerId);
            case SHIPMENT -> shipmentRepository.existsById(ownerId);
            case VESSEL, CARGO, PAYMENT, REVIEW, OTHER -> true; // catalog owners validated loosely
        };
        if (!exists) {
            throw new BadRequestException("Unknown owner " + ownerType + "#" + ownerId);
        }
    }

    /**
     * Resolves the owning client for a record and compares it with the
     * requesting user's client profile - throws 404 on mismatch so foreign
     * documents are indistinguishable from missing ones.
     */
    private void requireOwnership(String email, DocumentOwnerType ownerType, Long ownerId) {
        Client requester = requireClient(email);
        Long clientId = switch (ownerType) {
            case ENQUIRY -> enquiryRepository.findById(ownerId)
                    .map(Enquiry::getClient).map(Client::getId).orElse(null);
            case QUOTATION -> quotationRepository.findById(ownerId)
                    .map(Quotation::getClient).map(Client::getId).orElse(null);
            case INVOICE -> invoiceRepository.findById(ownerId)
                    .map(Invoice::getClient).map(Client::getId).orElse(null);
            case SHIPMENT -> shipmentRepository.findById(ownerId)
                    .map(Shipment::getClient).map(Client::getId).orElse(null);
            default -> null;
        };
        if (clientId == null || !clientId.equals(requester.getId())) {
            throw new ResourceNotFoundException("Document not found");
        }
    }

    private List<Document> allOwnedDocuments(String email) {
        Client requester = requireClient(email);
        List<Long> enquiryIds = enquiryRepository
                .findByClientId(requester.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(Enquiry::getId).toList();
        List<Long> invoiceIds = invoiceRepository.findByClientId(requester.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .map(Invoice::getId).toList();
        List<Long> shipmentIds = shipmentRepository
                .findByClientId(requester.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(Shipment::getId).toList();
        List<Document> owned = new java.util.ArrayList<>();
        owned.addAll(collect(DocumentOwnerType.ENQUIRY, enquiryIds));
        owned.addAll(collect(DocumentOwnerType.INVOICE, invoiceIds));
        owned.addAll(collect(DocumentOwnerType.SHIPMENT, shipmentIds));
        owned.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return owned;
    }

    private List<Document> collect(DocumentOwnerType type, List<Long> ids) {
        return ids.stream()
                .flatMap(id -> documentRepository
                        .findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(type, id).stream())
                .toList();
    }

    private static DocumentCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return DocumentCategory.OTHER;
        }
        try {
            return DocumentCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown document category: " + raw
                    + " (allowed: " + Arrays.toString(DocumentCategory.values()) + ")");
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 && dot < filename.length() - 1
                ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private DocumentResponse toResponse(Document document) {
        User uploader = document.getUploadedBy();
        return new DocumentResponse(
                document.getId(),
                document.getOwnerType().name(),
                document.getOwnerId(),
                document.getCategory().name(),
                document.getTitle(),
                document.getFileFormat(),
                document.getFileSizeBytes(),
                uploader != null ? uploader.getEmail() : null,
                document.getCreatedAt(),
                "/api/documents/" + document.getId());
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Client requireClient(String email) {
        User user = requireUser(email);
        return clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    public record StoredDownload(String filename, byte[] bytes) {
    }
}
