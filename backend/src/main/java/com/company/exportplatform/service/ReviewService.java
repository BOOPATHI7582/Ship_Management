package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ReviewRequest;
import com.company.exportplatform.dto.response.ReviewResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Review;
import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.ReviewRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post-delivery reviews: only the owning client may review, only COMPLETED
 * shipments are reviewable, one review per shipment. Public visibility is
 * gated by manager moderation (approved flag).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ShipmentRepository shipmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ---------- client side ----------

    @Transactional
    public ReviewResponse create(String email, ReviewRequest request) {
        Client client = requireClient(email);
        Shipment shipment = shipmentRepository.findById(request.shipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (shipment.getClient() == null || !client.getId().equals(shipment.getClient().getId())) {
            throw new ResourceNotFoundException("Shipment not found");
        }
        if (shipment.getStatus() != ShipmentStatus.COMPLETED) {
            throw new BadRequestException("Only completed shipments can be reviewed");
        }
        if (reviewRepository.existsByShipmentId(shipment.getId())) {
            throw new BadRequestException("This shipment has already been reviewed");
        }

        Review review = new Review();
        review.setShipment(shipment);
        review.setClient(client);
        review.setRating(request.rating());
        review.setTitle(trimToNull(request.title()));
        review.setReviewText(trimToNull(request.reviewText()));
        review.setApproved(false);

        Review saved = reviewRepository.save(review);
        log.info("Review {} submitted for shipment {} by client {}",
                saved.getId(), shipment.getShipmentRef(), email);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listMine(String email, Pageable pageable) {
        Client client = requireClient(email);
        return reviewRepository.findAll((root, query, cb) ->
                        cb.equal(root.get("client").get("id"), client.getId()), pageable)
                .map(this::toResponse);
    }

    /** Whether the client already reviewed a given shipment (for UI gating). */
    @Transactional(readOnly = true)
    public boolean hasReviewed(String email, Long shipmentId) {
        return reviewRepository.existsByShipmentId(shipmentId);
    }

    // ---------- staff side ----------

    @Transactional(readOnly = true)
    public Page<ReviewResponse> list(String moderation, String search, Pageable pageable) {
        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (moderation != null && !moderation.isBlank()) {
                switch (moderation.trim().toUpperCase()) {
                    case "PENDING" -> predicates.add(cb.isFalse(root.get("approved")));
                    case "APPROVED" -> predicates.add(cb.isTrue(root.get("approved")));
                    case "ALL" -> { }
                    default -> throw new BadRequestException("Unknown moderation filter: " + moderation);
                }
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("reviewText")), like),
                        cb.like(cb.lower(root.get("shipment").get("shipmentRef")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("companyName")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return reviewRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse moderate(Long id, boolean approved, String staffEmail) {
        User moderator = requireUser(staffEmail);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setApproved(approved);
        review.setModeratedBy(moderator);
        review.setModeratedAt(LocalDateTime.now());
        log.info("Review {} {} by {}", id, approved ? "approved" : "rejected", staffEmail);
        auditService.record(staffEmail, approved ? "REVIEW_APPROVED" : "REVIEW_REJECTED",
                "REVIEW", id, null,
                java.util.Map.of("shipmentId", review.getShipment() != null
                        ? String.valueOf(review.getShipment().getId()) : "-"));
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return reviewRepository.count((root, query, cb) -> cb.isFalse(root.get("approved")));
    }

    // ---------- helpers ----------

    private ReviewResponse toResponse(Review review) {
        Shipment shipment = review.getShipment();
        Client client = review.getClient();
        User clientUser = client != null ? client.getUser() : null;
        User moderator = review.getModeratedBy();
        String clientName = clientUser == null ? null
                : clientUser.getCompanyName() != null && !clientUser.getCompanyName().isBlank()
                        ? clientUser.getCompanyName() : clientUser.getFullName();
        return new ReviewResponse(
                review.getId(),
                shipment != null ? shipment.getId() : null,
                shipment != null ? shipment.getShipmentRef() : null,
                client != null ? client.getId() : null,
                clientName,
                review.getRating(),
                review.getTitle(),
                review.getReviewText(),
                review.isApproved(),
                moderator != null ? moderator.getEmail() : null,
                review.getModeratedAt(),
                review.getCreatedAt());
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

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
