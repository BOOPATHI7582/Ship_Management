package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ExportEnquiryRequest;
import com.company.exportplatform.dto.response.EnquiryResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.CargoCategory;
import com.company.exportplatform.entity.Enquiry;
import com.company.exportplatform.entity.Port;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.CargoCategoryRepository;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.EnquiryRepository;
import com.company.exportplatform.repository.PortRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ClientRepository clientRepository;
    private final CargoCategoryRepository cargoCategoryRepository;
    private final PortRepository portRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGenerator documentNumberGenerator;

    @Transactional
    public EnquiryResponse create(String email, ExportEnquiryRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No client profile found for this account. Please contact support."));

        if (request.getRequiredLoadingDate() != null && request.getExpectedDeliveryDate() != null
                && request.getExpectedDeliveryDate().isBefore(request.getRequiredLoadingDate())) {
            throw new BadRequestException("Expected delivery date cannot be before the required loading date");
        }

        Enquiry enquiry = new Enquiry();
        enquiry.setReferenceNo(documentNumberGenerator.next("ENQUIRY", "ENQ"));
        enquiry.setClient(client);
        applyContactDetails(enquiry, request, user);
        enquiry.setCargoType(request.getCargoType().trim());
        if (request.getCargoCategoryId() != null) {
            CargoCategory category = cargoCategoryRepository.findById(request.getCargoCategoryId())
                    .orElseThrow(() -> new BadRequestException("Selected cargo category does not exist"));
            enquiry.setCargoCategory(category);
        }
        enquiry.setCargoDescription(trimOrNull(request.getCargoDescription()));
        enquiry.setQuantity(request.getQuantity());
        enquiry.setUnit(defaultIfBlank(request.getUnit(), "MT"));
        enquiry.setOriginCountry(request.getOriginCountry().trim());
        enquiry.setOriginLocation(trimOrNull(request.getOriginLocation()));
        enquiry.setLoadingPort(resolvePort(request.getLoadingPortId()));
        enquiry.setDestinationCountry(request.getDestinationCountry().trim());
        enquiry.setDestinationLocation(trimOrNull(request.getDestinationLocation()));
        enquiry.setDestinationPort(resolvePort(request.getDestinationPortId()));
        enquiry.setRequiredLoadingDate(request.getRequiredLoadingDate());
        enquiry.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        enquiry.setCurrency(defaultIfBlank(request.getCurrency(), "INR").toUpperCase());
        enquiry.setEstimatedBudget(request.getEstimatedBudget());
        enquiry.setTargetPricePerUnit(request.getTargetPricePerUnit());
        enquiry.setMessage(trimOrNull(request.getMessage()));

        Enquiry saved = enquiryRepository.save(enquiry);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EnquiryResponse> listForClient(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = requireClient(user.getId());
        return enquiryRepository.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EnquiryResponse getOwned(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = requireClient(user.getId());
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        if (!enquiry.getClient().getId().equals(client.getId())) {
            throw new ResourceNotFoundException("Enquiry not found");
        }
        return toResponse(enquiry);
    }

    private void applyContactDetails(Enquiry enquiry, ExportEnquiryRequest request, User user) {
        enquiry.setContactName(request.getContactName() != null ? request.getContactName().trim() : user.getFullName());
        enquiry.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim().toLowerCase() : user.getEmail());
        enquiry.setContactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : user.getPhone());
    }

    private Port resolvePort(Long portId) {
        if (portId == null) {
            return null;
        }
        Port port = portRepository.findById(portId)
                .orElseThrow(() -> new BadRequestException("Selected port does not exist"));
        return port;
    }

    private Client requireClient(Long userId) {
        return clientRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No client profile found for this account"));
    }

    private String trimOrNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private EnquiryResponse toResponse(Enquiry enquiry) {
        return new EnquiryResponse(
                enquiry.getId(),
                enquiry.getReferenceNo(),
                enquiry.getStatus().name(),
                enquiry.getCargoType(),
                enquiry.getCargoCategory() != null ? enquiry.getCargoCategory().getName() : null,
                enquiry.getCargoDescription(),
                enquiry.getQuantity() == null ? null : enquiry.getQuantity().longValue(),
                enquiry.getUnit(),
                enquiry.getOriginCountry(),
                enquiry.getOriginLocation(),
                enquiry.getLoadingPort() != null ? enquiry.getLoadingPort().getName() : null,
                enquiry.getLoadingPort() != null ? enquiry.getLoadingPort().getCode() : null,
                enquiry.getDestinationCountry(),
                enquiry.getDestinationLocation(),
                enquiry.getDestinationPort() != null ? enquiry.getDestinationPort().getName() : null,
                enquiry.getDestinationPort() != null ? enquiry.getDestinationPort().getCode() : null,
                enquiry.getRequiredLoadingDate() == null ? null : enquiry.getRequiredLoadingDate().toString(),
                enquiry.getExpectedDeliveryDate() == null ? null : enquiry.getExpectedDeliveryDate().toString(),
                enquiry.getCurrency(),
                enquiry.getEstimatedBudget(),
                enquiry.getTargetPricePerUnit(),
                enquiry.getMessage(),
                enquiry.getCreatedAt() == null ? null : enquiry.getCreatedAt().toString()
        );
    }
}
