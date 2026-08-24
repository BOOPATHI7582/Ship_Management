package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.PublicCargoResponse;
import com.company.exportplatform.dto.response.PublicCategoryResponse;
import com.company.exportplatform.dto.response.PublicPortResponse;
import com.company.exportplatform.dto.response.PublicReviewResponse;
import com.company.exportplatform.dto.response.PublicStatsResponse;
import com.company.exportplatform.dto.response.PublicTrackingResponse;
import com.company.exportplatform.entity.Cargo;
// import com.company.exportplatform.entity.CargoCategory;
import com.company.exportplatform.entity.Port;
// import com.company.exportplatform.entity.Review;
import com.company.exportplatform.entity.Shipment;
import com.company.exportplatform.entity.ShipmentTracking;
import com.company.exportplatform.entity.enums.CargoStatus;
import com.company.exportplatform.entity.enums.ShipmentStatus;
import com.company.exportplatform.entity.enums.VesselStatus;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.CargoCategoryRepository;
import com.company.exportplatform.repository.CargoRepository;
import com.company.exportplatform.repository.PortRepository;
import com.company.exportplatform.repository.ReviewRepository;
import com.company.exportplatform.repository.ShipmentRepository;
import com.company.exportplatform.repository.ShipmentTrackingRepository;
import com.company.exportplatform.repository.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final CargoRepository cargoRepository;
    private final CargoCategoryRepository cargoCategoryRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final ReviewRepository reviewRepository;
    private final VesselRepository vesselRepository;
    private final PortRepository portRepository;

    @Transactional(readOnly = true)
    public List<PublicCargoResponse> availableCargo() {
        return cargoRepository.findByStatus(CargoStatus.AVAILABLE).stream()
                .map(this::toCargoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> activeCategories() {
        return cargoCategoryRepository.findByActiveTrue().stream()
                .map(category -> new PublicCategoryResponse(
                        category.getId(), category.getName(), category.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicPortResponse> activePorts() {
        return portRepository.findByActiveTrue().stream()
                .map(port -> new PublicPortResponse(
                        port.getId(), port.getName(), port.getCode(),
                        port.getCountry(), port.getCity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicTrackingResponse trackByRef(String shipmentRef) {
        Shipment shipment = shipmentRepository.findByShipmentRef(shipmentRef)
                .orElseThrow(() -> new ResourceNotFoundException("No shipment found for reference " + shipmentRef));

        List<PublicTrackingResponse.TimelineEntry> timeline =
                shipmentTrackingRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()).stream()
                        .map(this::toTimelineEntry)
                        .toList();

        return new PublicTrackingResponse(
                shipment.getShipmentRef(),
                shipment.getStatus().name(),
                shipment.getVessel() != null ? shipment.getVessel().getName() : null,
                shipment.getVessel() != null ? shipment.getVessel().getVesselType() : null,
                shipment.getCargo() != null ? shipment.getCargo().getName() : null,
                shipment.getCargo() != null && shipment.getCargo().getCategory() != null
                        ? shipment.getCargo().getCategory().getName() : null,
                shipment.getQuantity(),
                shipment.getUnit(),
                shipment.getLoadingPort() != null ? shipment.getLoadingPort().getName() : null,
                shipment.getLoadingPort() != null ? shipment.getLoadingPort().getCode() : null,
                shipment.getDestinationPort() != null ? shipment.getDestinationPort().getName() : null,
                shipment.getDestinationPort() != null ? shipment.getDestinationPort().getCode() : null,
                shipment.getCurrentLocation(),
                shipment.getCurrentLatitude(),
                shipment.getCurrentLongitude(),
                shipment.getLoadingDate(),
                shipment.getEstimatedArrival(),
                timeline
        );
    }

    @Transactional(readOnly = true)
    public List<PublicReviewResponse> approvedReviews() {
        return reviewRepository.findByApprovedTrue().stream()
                .map(review -> new PublicReviewResponse(
                        review.getId(),
                        review.getRating(),
                        review.getTitle(),
                        review.getReviewText(),
                        review.getClient() != null && review.getClient().getUser() != null
                                ? review.getClient().getUser().getFullName() : "Verified Client",
                        review.getClient() != null && review.getClient().getUser() != null
                                ? review.getClient().getUser().getCompanyName() : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicStatsResponse stats() {
        return new PublicStatsResponse(
                vesselRepository.countByStatusIn(Arrays.asList(
                        VesselStatus.AVAILABLE, VesselStatus.LOADING, VesselStatus.LOADING_COMPLETED,
                        VesselStatus.IN_TRANSIT, VesselStatus.ARRIVED)),
                portRepository.countByActiveTrue(),
                cargoRepository.countByStatus(CargoStatus.AVAILABLE),
                shipmentRepository.countByStatus(ShipmentStatus.DELIVERED));
    }

    private PublicCargoResponse toCargoResponse(Cargo cargo) {
        Port loadingPort = cargo.getLoadingPort();
        Port destinationPort = cargo.getDestinationPort();
        return new PublicCargoResponse(
                cargo.getId(),
                cargo.getName(),
                cargo.getCategory() != null ? cargo.getCategory().getName() : null,
                cargo.getQuantity(),
                cargo.getUnit(),
                cargo.getOriginCountry(),
                cargo.getDestinationCountry(),
                loadingPort != null ? loadingPort.getName() : null,
                loadingPort != null ? loadingPort.getCode() : null,
                destinationPort != null ? destinationPort.getName() : null,
                destinationPort != null ? destinationPort.getCode() : null,
                cargo.getLoadingDate(),
                cargo.getEstimatedArrival(),
                cargo.getIndicativePrice(),
                cargo.getCurrency()
        );
    }

    private PublicTrackingResponse.TimelineEntry toTimelineEntry(ShipmentTracking tracking) {
        return new PublicTrackingResponse.TimelineEntry(
                tracking.getStatus(),
                tracking.getLocationLabel(),
                tracking.getOccurredAt(),
                tracking.getNotes()
        );
    }
}
