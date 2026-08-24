package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ShipmentProgressRequest;
import com.company.exportplatform.dto.request.ShipmentRequest;
import com.company.exportplatform.dto.request.ShipmentUpdateRequest;
import com.company.exportplatform.dto.response.ShipmentResponse;
import com.company.exportplatform.dto.response.ShipmentTrackingResponse;
import com.company.exportplatform.entity.Cargo;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.Port;
import com.company.exportplatform.entity.Quotation;
import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.ShipmentTracking;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.Vessel;
import com.company.exportplatform.entity.enums.NotificationType;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.CargoRepository;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.PortRepository;
import com.company.exportplatform.repository.QuotationRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.ShipmentTrackingRepository;
import com.company.exportplatform.repository.UserRepository;
import com.company.exportplatform.repository.VesselRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Operational shipment lifecycle: forward-only 11-state machine validated
 * server-side. Every progress update appends an immutable tracking entry,
 * refreshes the current-location snapshot and notifies the client.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final ClientRepository clientRepository;
    private final QuotationRepository quotationRepository;
    private final EnquiryRepository enquiryRepository;
    private final CargoRepository cargoRepository;
    private final VesselRepository vesselRepository;
    private final PortRepository portRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // ---------- manager side ----------

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> list(String status, String search, Pageable pageable) {
        Specification<Shipment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), ShipmentStatus.valueOf(status)));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid shipment status");
                }
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("shipmentRef")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("fullName")), like),
                        cb.like(cb.lower(root.get("client").get("user").get("companyName")), like),
                        cb.like(cb.lower(root.get("currentLocation")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return shipmentRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public ShipmentResponse create(String staffEmail, ShipmentRequest request) {
        User staff = requireUser(staffEmail);

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Quotation quotation = null;
        if (request.quotationId() != null) {
            quotation = quotationRepository.findById(request.quotationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
            if (quotation.getClient() == null || !client.getId().equals(quotation.getClient().getId())) {
                throw new BadRequestException("Quotation does not belong to the selected client");
            }
        }

        Enquiry enquiry = null;
        if (request.enquiryId() != null) {
            enquiry = enquiryRepository.findById(request.enquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
            if (enquiry.getClient() == null || !client.getId().equals(enquiry.getClient().getId())) {
                throw new BadRequestException("Enquiry does not belong to the selected client");
            }
        }

        Shipment shipment = new Shipment();
        shipment.setShipmentRef(documentNumberGenerator.next("SHIPMENT", "SHP"));
        shipment.setTrackingToken(UUID.randomUUID().toString().replace("-", ""));
        shipment.setClient(client);
        shipment.setQuotation(quotation);
        shipment.setEnquiry(enquiry);
        shipment.setCargo(resolveCargo(request.cargoId()));
        shipment.setVessel(resolveVessel(request.vesselId()));
        shipment.setQuantity(scale(request.quantity()));
        shipment.setUnit(trimToNull(request.unit()));
        shipment.setOriginCountry(trimToNull(request.originCountry()));
        shipment.setDestinationCountry(trimToNull(request.destinationCountry()));
        shipment.setLoadingPort(resolvePort(request.loadingPortId(), "Loading port"));
        shipment.setDestinationPort(resolvePort(request.destinationPortId(), "Destination port"));
        shipment.setLoadingDate(request.loadingDate());
        shipment.setEstimatedArrival(request.estimatedArrival());
        shipment.setFinalPrice(scale(request.finalPrice()));
        shipment.setCurrency(firstNonBlank(request.currency(), "INR"));
        shipment.setNotes(trimToNull(request.notes()));
        shipment.setStatus(ShipmentStatus.BOOKING_CONFIRMED);

        if (request.loadingDate() != null && request.estimatedArrival() != null
                && request.estimatedArrival().isBefore(request.loadingDate())) {
            throw new BadRequestException("Estimated arrival cannot be before the loading date");
        }

        Shipment saved = shipmentRepository.save(shipment);
        appendTracking(saved, staff, ShipmentStatus.BOOKING_CONFIRMED,
                firstNonBlank(shipment.getOriginCountry(), "Origin"), LocalDateTime.now(),
                "Shipment booking confirmed");

        notifyClient(client, "Shipment " + saved.getShipmentRef() + " booked",
                "Your shipment is confirmed. Track it any time with reference "
                        + saved.getShipmentRef() + ".",
                "/client/shipments/" + saved.getId());

        log.info("Shipment {} created for client {} by {}",
                saved.getShipmentRef(), client.getId(), staffEmail);
        auditService.record(staffEmail, "SHIPMENT_CREATED", "SHIPMENT", saved.getId(),
                null, java.util.Map.of("shipmentRef", saved.getShipmentRef(),
                        "clientId", client.getId()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse detailForManager(Long id) {
        return toResponse(findShipment(id));
    }

    @Transactional
    public ShipmentResponse update(Long id, ShipmentUpdateRequest request) {
        Shipment shipment = findShipment(id);
        if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.COMPLETED) {
            throw new BadRequestException("A delivered or completed shipment can no longer be edited");
        }
        if (request.cargoId() != null) {
            shipment.setCargo(resolveCargo(request.cargoId()));
        }
        if (request.vesselId() != null) {
            shipment.setVessel(resolveVessel(request.vesselId()));
        }
        if (request.loadingPortId() != null) {
            shipment.setLoadingPort(resolvePort(request.loadingPortId(), "Loading port"));
        }
        if (request.destinationPortId() != null) {
            shipment.setDestinationPort(resolvePort(request.destinationPortId(), "Destination port"));
        }
        if (request.loadingDate() != null) {
            shipment.setLoadingDate(request.loadingDate());
        }
        if (request.estimatedArrival() != null) {
            shipment.setEstimatedArrival(request.estimatedArrival());
        }
        if (shipment.getLoadingDate() != null && shipment.getEstimatedArrival() != null
                && shipment.getEstimatedArrival().isBefore(shipment.getLoadingDate())) {
            throw new BadRequestException("Estimated arrival cannot be before the loading date");
        }
        if (request.finalPrice() != null) {
            shipment.setFinalPrice(scale(request.finalPrice()));
        }
        if (request.notes() != null) {
            shipment.setNotes(trimToNull(request.notes()));
        }
        log.info("Shipment {} assignments updated", shipment.getShipmentRef());
        return toResponse(shipment);
    }

    /**
     * Appends a tracking point and optionally advances the lifecycle state.
     * Transitions are forward-only; a null status logs a position update only.
     */
    @Transactional
    public ShipmentResponse progress(Long id, String staffEmail, ShipmentProgressRequest request) {
        User staff = requireUser(staffEmail);
        Shipment shipment = findShipment(id);

        ShipmentStatus target = shipment.getStatus();
        if (request.status() != null && !request.status().isBlank()) {
            target = parseStatus(request.status());
            if (target.ordinal() < shipment.getStatus().ordinal()) {
                throw new BadRequestException("Shipments move forward only - cannot go back to "
                        + target.name() + " from " + shipment.getStatus().name());
            }
            if (target == shipment.getStatus()
                    && shipment.getStatus() == ShipmentStatus.COMPLETED) {
                // logging more points after completion is allowed, no state change
            } else if (target.ordinal() > shipment.getStatus().ordinal()) {
                shipment.setStatus(target);
            }
        }

        LocalDateTime occurredAt = request.occurredAt() != null ? request.occurredAt() : LocalDateTime.now();
        appendTracking(shipment, staff, target,
                firstNonBlank(request.locationLabel(), shipment.getCurrentLocation()),
                occurredAt, trimToNull(request.notes()));

        shipment.setCurrentLocation(firstNonBlank(request.locationLabel(), shipment.getCurrentLocation()));
        if (request.latitude() != null) {
            shipment.setCurrentLatitude(scale(request.latitude()));
        }
        if (request.longitude() != null) {
            shipment.setCurrentLongitude(scale(request.longitude()));
        }
        shipment.setLastTrackedAt(LocalDateTime.now());

        if (target == ShipmentStatus.ARRIVED && shipment.getActualArrival() == null) {
            shipment.setActualArrival(LocalDate.now());
        }
        if (target == ShipmentStatus.DELIVERED && shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        notifyClient(shipment.getClient(),
                "Shipment " + shipment.getShipmentRef() + " - " + label(target),
                firstNonBlank(request.locationLabel(), "Status updated to " + label(target)) + ".",
                "/client/shipments/" + shipment.getId());

        log.info("Shipment {} progress: {} at {} by {}",
                shipment.getShipmentRef(), target.name(),
                firstNonBlank(request.locationLabel(), "-"), staffEmail);
        auditService.record(staffEmail, "SHIPMENT_PROGRESS", "SHIPMENT", shipment.getId(),
                null, java.util.Map.of("shipmentRef", shipment.getShipmentRef(),
                        "status", target.name()));
        return toResponse(shipment);
    }

    // ---------- client side ----------

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> listMine(String email, Pageable pageable) {
        Client client = requireClient(email);
        return shipmentRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse detailForClient(String email, Long id) {
        return toResponse(ownedShipment(email, id));
    }

    // ---------- helpers ----------

    private void appendTracking(Shipment shipment, User recorder, ShipmentStatus status,
                                String locationLabel, LocalDateTime occurredAt, String notes) {
        ShipmentTracking entry = new ShipmentTracking();
        entry.setShipment(shipment);
        entry.setStatus(status);
        entry.setLocationLabel(locationLabel);
        entry.setLatitude(shipment.getCurrentLatitude());
        entry.setLongitude(shipment.getCurrentLongitude());
        entry.setOccurredAt(occurredAt);
        entry.setNotes(notes);
        entry.setRecordedBy(recorder);
        trackingRepository.save(entry);
    }

    private static ShipmentStatus parseStatus(String raw) {
        try {
            return ShipmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown shipment status: " + raw);
        }
    }

    private Cargo resolveCargo(Long id) {
        if (id == null) {
            return null;
        }
        return cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo lot not found"));
    }

    private Vessel resolveVessel(Long id) {
        if (id == null) {
            return null;
        }
        return vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel not found"));
    }

    private Port resolvePort(Long id, String label) {
        if (id == null) {
            return null;
        }
        return portRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found"));
    }

    private void notifyClient(Client client, String title, String message, String link) {
        if (client != null && client.getUser() != null) {
            notificationService.notify(client.getUser(), NotificationType.SHIPMENT, title, message, link,
                    "SHIPMENT", client.getId());
        }
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        List<ShipmentTrackingResponse> timeline =
                trackingRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()).stream()
                        .map(t -> new ShipmentTrackingResponse(t.getId(),
                                t.getStatus() != null ? t.getStatus().name() : null,
                                t.getLocationLabel(), t.getLatitude(), t.getLongitude(),
                                t.getOccurredAt(), t.getNotes()))
                        .toList();

        Client client = shipment.getClient();
        User clientUser = client != null ? client.getUser() : null;
        Quotation quotation = shipment.getQuotation();
        Cargo cargo = shipment.getCargo();
        Vessel vessel = shipment.getVessel();
        Port loadingPort = shipment.getLoadingPort();
        Port destinationPort = shipment.getDestinationPort();

        return new ShipmentResponse(
                shipment.getId(), shipment.getShipmentRef(), shipment.getStatus().name(),
                client != null ? client.getId() : null,
                clientUser != null && clientUser.getCompanyName() != null && !clientUser.getCompanyName().isBlank()
                        ? clientUser.getCompanyName()
                        : clientUser != null ? clientUser.getFullName() : null,
                clientUser != null ? clientUser.getEmail() : null,
                quotation != null ? quotation.getId() : null,
                quotation != null ? quotation.getQuoteNo() : null,
                shipment.getEnquiry() != null ? shipment.getEnquiry().getId() : null,
                cargo != null ? cargo.getId() : null,
                cargo != null ? cargo.getName() : null,
                vessel != null ? vessel.getId() : null,
                vessel != null ? vessel.getName() : null,
                vessel != null ? vessel.getImoNumber() : null,
                shipment.getQuantity(), shipment.getUnit(),
                shipment.getOriginCountry(), shipment.getDestinationCountry(),
                loadingPort != null ? loadingPort.getId() : null,
                loadingPort != null ? loadingPort.getName() : null,
                loadingPort != null ? loadingPort.getCode() : null,
                destinationPort != null ? destinationPort.getId() : null,
                destinationPort != null ? destinationPort.getName() : null,
                destinationPort != null ? destinationPort.getCode() : null,
                shipment.getLoadingDate(), shipment.getEstimatedArrival(),
                shipment.getActualArrival(), shipment.getDeliveredAt(),
                shipment.getFinalPrice(), shipment.getCurrency(),
                shipment.getCurrentLocation(), shipment.getCurrentLatitude(), shipment.getCurrentLongitude(),
                shipment.getLastTrackedAt(),
                shipment.getNotes(), shipment.getCreatedAt(), timeline);
    }

    private static String label(ShipmentStatus status) {
        return status != null ? status.name().replace('_', ' ') : "";
    }

    private Shipment findShipment(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
    }

    private Shipment ownedShipment(String email, Long id) {
        Client client = requireClient(email);
        Shipment shipment = findShipment(id);
        if (shipment.getClient() == null || !client.getId().equals(shipment.getClient().getId())) {
            throw new ResourceNotFoundException("Shipment not found");
        }
        return shipment;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Client requireClient(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        return b != null && !b.isBlank() ? b.trim() : null;
    }
}
