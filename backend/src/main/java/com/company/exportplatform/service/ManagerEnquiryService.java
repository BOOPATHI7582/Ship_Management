package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.EnquiryStatusRequest;
import com.company.exportplatform.dto.response.ManagerEnquiryResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.EnquiryStatus;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.EnquiryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ManagerEnquiryService {

    private static final Set<EnquiryStatus> MANAGER_SETTABLE =
            Set.of(EnquiryStatus.REVIEWING, EnquiryStatus.CONTACTED, EnquiryStatus.REJECTED, EnquiryStatus.CLOSED);

    private final EnquiryRepository enquiryRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ManagerEnquiryResponse> list(String status, String search, Pageable pageable) {
        Specification<Enquiry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), EnquiryStatus.valueOf(status)));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("referenceNo")), term),
                        cb.like(cb.lower(root.get("cargoType")), term),
                        cb.like(cb.lower(root.get("client").get("user").get("fullName")), term),
                        cb.like(cb.lower(root.get("client").get("user").get("email")), term)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return enquiryRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ManagerEnquiryResponse detail(Long id) {
        return toResponse(findEnquiry(id));
    }

    @Transactional
    public ManagerEnquiryResponse updateStatus(Long id, EnquiryStatusRequest request) {
        Enquiry enquiry = findEnquiry(id);
        EnquiryStatus newStatus;
        try {
            newStatus = EnquiryStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid enquiry status");
        }
        if (!MANAGER_SETTABLE.contains(newStatus)) {
            throw new BadRequestException("Status " + newStatus + " cannot be set manually");
        }
        enquiry.setStatus(newStatus);

        String title;
        String message;
        switch (newStatus) {
            case REVIEWING -> { title = "Enquiry under review"; message = "Your enquiry " + enquiry.getReferenceNo() + " is being reviewed by our operations team."; }
            case CONTACTED -> { title = "We contacted you"; message = "Our team has responded regarding enquiry " + enquiry.getReferenceNo() + ". Check the negotiation thread."; }
            case REJECTED -> { title = "Enquiry declined"; message = "Unfortunately your enquiry " + enquiry.getReferenceNo() + " could not be taken forward."; }
            default -> { title = "Enquiry closed"; message = "Your enquiry " + enquiry.getReferenceNo() + " has been closed."; }
        }
        notifyClient(enquiry, NotificationType.ENQUIRY, title, message, "/client/enquiries/" + enquiry.getId(), enquiry.getId());
        return toResponse(enquiry);
    }

    private void notifyClient(Enquiry enquiry, NotificationType type, String title, String message, String link, Long entityId) {
        Client client = enquiry.getClient();
        User user = client != null ? client.getUser() : null;
        if (user != null) {
            notificationService.notify(user, type, title, message, link, "ENQUIRY", entityId);
        }
    }

    private Enquiry findEnquiry(Long id) {
        return enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
    }

    private ManagerEnquiryResponse toResponse(Enquiry enquiry) {
        Client client = enquiry.getClient();
        User user = client != null ? client.getUser() : null;
        return new ManagerEnquiryResponse(
                enquiry.getId(),
                enquiry.getReferenceNo(),
                enquiry.getStatus().name(),
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getCompanyName() : null,
                enquiry.getContactName(),
                enquiry.getContactPhone(),
                enquiry.getCargoType(),
                enquiry.getCargoCategory() != null ? enquiry.getCargoCategory().getName() : null,
                enquiry.getCargoDescription(),
                enquiry.getQuantity(),
                enquiry.getUnit(),
                enquiry.getOriginCountry(),
                enquiry.getDestinationCountry(),
                enquiry.getLoadingPort() != null ? enquiry.getLoadingPort().getName() : null,
                enquiry.getDestinationPort() != null ? enquiry.getDestinationPort().getName() : null,
                enquiry.getCurrency(),
                enquiry.getEstimatedBudget(),
                enquiry.getTargetPricePerUnit(),
                enquiry.getMessage(),
                enquiry.getCreatedAt()
        );
    }
}
