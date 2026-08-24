package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.CargoRequest;
import com.company.exportplatform.dto.request.CategoryRequest;
import com.company.exportplatform.dto.request.PortRequest;
import com.company.exportplatform.dto.request.VesselRequest;
import com.company.exportplatform.dto.response.CargoAdminResponse;
import com.company.exportplatform.dto.response.CategoryAdminResponse;
import com.company.exportplatform.dto.response.PortAdminResponse;
import com.company.exportplatform.dto.response.VesselAdminResponse;
import com.company.exportplatform.entity.Cargo;
import com.company.exportplatform.entity.CargoCategory;
import com.company.exportplatform.entity.Port;
import com.company.exportplatform.entity.Vessel;
import com.company.exportplatform.entity.enums.CargoStatus;
import com.company.exportplatform.entity.enums.VesselStatus;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.CargoCategoryRepository;
import com.company.exportplatform.repository.CargoRepository;
import com.company.exportplatform.repository.PortRepository;
import com.company.exportplatform.repository.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final VesselRepository vesselRepository;
    private final CargoRepository cargoRepository;
    private final CargoCategoryRepository cargoCategoryRepository;
    private final PortRepository portRepository;

    // ---------- Vessels ----------

    @Transactional(readOnly = true)
    public Page<VesselAdminResponse> listVessels(String status, Pageable pageable) {
        Page<Vessel> page = (status == null || status.isBlank())
                ? vesselRepository.findAll(pageable)
                : vesselRepository.findByStatus(VesselStatus.valueOf(status), pageable);
        return page.map(this::toVesselResponse);
    }

    @Transactional
    public VesselAdminResponse createVessel(VesselRequest request) {
        if (!isNotBlank(request.getName())) {
            throw new BadRequestException("Vessel name is required");
        }
        if (!isNotBlank(request.getVesselType())) {
            throw new BadRequestException("Vessel type is required");
        }
        Vessel vessel = new Vessel();
        applyVessel(vessel, request);
        return toVesselResponse(vesselRepository.save(vessel));
    }

    @Transactional
    public VesselAdminResponse updateVessel(Long id, VesselRequest request) {
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel not found"));
        if (isNotBlank(request.getName())) {
            vessel.setName(request.getName().trim());
        }
        if (request.getImoNumber() != null) {
            vessel.setImoNumber(blankToNull(request.getImoNumber()));
        }
        if (isNotBlank(request.getVesselType())) {
            vessel.setVesselType(request.getVesselType().trim());
        }
        if (request.getCapacity() != null) {
            vessel.setCapacity(request.getCapacity());
        }
        if (request.getCapacityUnit() != null && !request.getCapacityUnit().isBlank()) {
            vessel.setCapacityUnit(request.getCapacityUnit());
        }
        if (request.getFlag() != null) {
            vessel.setFlag(blankToNull(request.getFlag()));
        }
        if (request.getCurrentLocation() != null) {
            vessel.setCurrentLocation(blankToNull(request.getCurrentLocation()));
        }
        if (isNotBlank(request.getStatus())) {
            vessel.setStatus(VesselStatus.valueOf(request.getStatus()));
        }
        if (request.getManagementCompany() != null) {
            vessel.setManagementCompany(blankToNull(request.getManagementCompany()));
        }
        if (request.getManagementContact() != null) {
            vessel.setManagementContact(blankToNull(request.getManagementContact()));
        }
        if (request.getDescription() != null) {
            vessel.setDescription(blankToNull(request.getDescription()));
        }
        return toVesselResponse(vesselRepository.save(vessel));
    }

    private void applyVessel(Vessel vessel, VesselRequest request) {
        vessel.setName(request.getName().trim());
        vessel.setImoNumber(blankToNull(request.getImoNumber()));
        vessel.setVesselType(request.getVesselType().trim());
        vessel.setCapacity(request.getCapacity());
        vessel.setCapacityUnit(request.getCapacityUnit());
        vessel.setFlag(blankToNull(request.getFlag()));
        vessel.setCurrentLocation(blankToNull(request.getCurrentLocation()));
        if (request.getStatus() != null) {
            vessel.setStatus(VesselStatus.valueOf(request.getStatus()));
        }
        vessel.setManagementCompany(blankToNull(request.getManagementCompany()));
        vessel.setManagementContact(blankToNull(request.getManagementContact()));
        vessel.setDescription(blankToNull(request.getDescription()));
    }

    private VesselAdminResponse toVesselResponse(Vessel vessel) {
        return new VesselAdminResponse(
                vessel.getId(),
                vessel.getName(),
                vessel.getImoNumber(),
                vessel.getVesselType(),
                vessel.getCapacity(),
                vessel.getCapacityUnit(),
                vessel.getFlag(),
                vessel.getCurrentLocation(),
                vessel.getStatus() != null ? vessel.getStatus().name() : null,
                vessel.getManagementCompany(),
                vessel.getManagementContact(),
                vessel.getDescription()
        );
    }

    // ---------- Cargo lots ----------

    @Transactional(readOnly = true)
    public Page<CargoAdminResponse> listCargo(String status, Pageable pageable) {
        Page<Cargo> page = (status == null || status.isBlank())
                ? cargoRepository.findAll(pageable)
                : cargoRepository.findByStatus(CargoStatus.valueOf(status), pageable);
        return page.map(this::toCargoResponse);
    }

    @Transactional
    public CargoAdminResponse createCargo(CargoRequest request) {
        if (!isNotBlank(request.getName())) {
            throw new BadRequestException("Cargo name is required");
        }
        if (request.getQuantity() == null) {
            throw new BadRequestException("Quantity is required");
        }
        if (!isNotBlank(request.getUnit())) {
            throw new BadRequestException("Unit is required");
        }
        if (!isNotBlank(request.getOriginCountry())) {
            throw new BadRequestException("Origin country is required");
        }
        if (!isNotBlank(request.getDestinationCountry())) {
            throw new BadRequestException("Destination country is required");
        }
        Cargo cargo = new Cargo();
        applyCargo(cargo, request);
        return toCargoResponse(cargoRepository.save(cargo));
    }

    @Transactional
    public CargoAdminResponse updateCargo(Long id, CargoRequest request) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo lot not found"));
        if (isNotBlank(request.getName())) {
            cargo.setName(request.getName().trim());
        }
        if (request.getCategoryId() != null) {
            CargoCategory category = cargoCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            cargo.setCategory(category);
        }
        if (request.getDescription() != null) {
            cargo.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getQuantity() != null) {
            cargo.setQuantity(request.getQuantity());
        }
        if (isNotBlank(request.getUnit())) {
            cargo.setUnit(request.getUnit());
        }
        if (isNotBlank(request.getOriginCountry())) {
            cargo.setOriginCountry(request.getOriginCountry().trim());
        }
        if (isNotBlank(request.getDestinationCountry())) {
            cargo.setDestinationCountry(request.getDestinationCountry().trim());
        }
        if (request.getLoadingPortId() != null) {
            cargo.setLoadingPort(portOrNull(request.getLoadingPortId(), "Loading port not found"));
        }
        if (request.getDestinationPortId() != null) {
            cargo.setDestinationPort(portOrNull(request.getDestinationPortId(), "Destination port not found"));
        }
        if (request.getLoadingDate() != null) {
            cargo.setLoadingDate(request.getLoadingDate());
        }
        if (request.getEstimatedArrival() != null) {
            cargo.setEstimatedArrival(request.getEstimatedArrival());
        }
        if (request.getIndicativePrice() != null) {
            cargo.setIndicativePrice(request.getIndicativePrice());
        }
        if (isNotBlank(request.getCurrency())) {
            cargo.setCurrency(request.getCurrency().trim());
        }
        if (isNotBlank(request.getStatus())) {
            cargo.setStatus(CargoStatus.valueOf(request.getStatus()));
        }
        return toCargoResponse(cargoRepository.save(cargo));
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void applyCargo(Cargo cargo, CargoRequest request) {
        cargo.setName(request.getName().trim());
        if (request.getCategoryId() != null) {
            CargoCategory category = cargoCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            cargo.setCategory(category);
        } else {
            cargo.setCategory(null);
        }
        cargo.setDescription(blankToNull(request.getDescription()));
        cargo.setQuantity(request.getQuantity());
        cargo.setUnit(request.getUnit());
        cargo.setOriginCountry(request.getOriginCountry().trim());
        cargo.setDestinationCountry(request.getDestinationCountry().trim());
        cargo.setLoadingPort(portOrNull(request.getLoadingPortId(), "Loading port not found"));
        cargo.setDestinationPort(portOrNull(request.getDestinationPortId(), "Destination port not found"));
        cargo.setLoadingDate(request.getLoadingDate());
        cargo.setEstimatedArrival(request.getEstimatedArrival());
        cargo.setIndicativePrice(request.getIndicativePrice());
        cargo.setCurrency(request.getCurrency() == null || request.getCurrency().isBlank()
                ? "INR" : request.getCurrency().trim());
        cargo.setStatus(CargoStatus.valueOf(request.getStatus()));
    }

    private Port portOrNull(Long portId, String message) {
        if (portId == null) {
            return null;
        }
        return portRepository.findById(portId)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private CargoAdminResponse toCargoResponse(Cargo cargo) {
        return new CargoAdminResponse(
                cargo.getId(),
                cargo.getName(),
                cargo.getCategory() != null ? cargo.getCategory().getId() : null,
                cargo.getCategory() != null ? cargo.getCategory().getName() : null,
                cargo.getDescription(),
                cargo.getQuantity(),
                cargo.getUnit(),
                cargo.getOriginCountry(),
                cargo.getDestinationCountry(),
                cargo.getLoadingPort() != null ? cargo.getLoadingPort().getId() : null,
                cargo.getLoadingPort() != null ? cargo.getLoadingPort().getName() : null,
                cargo.getDestinationPort() != null ? cargo.getDestinationPort().getId() : null,
                cargo.getDestinationPort() != null ? cargo.getDestinationPort().getName() : null,
                cargo.getLoadingDate(),
                cargo.getEstimatedArrival(),
                cargo.getIndicativePrice(),
                cargo.getCurrency(),
                cargo.getStatus() != null ? cargo.getStatus().name() : null
        );
    }

    // ---------- Categories ----------

    @Transactional(readOnly = true)
    public List<CategoryAdminResponse> listCategories() {
        return cargoCategoryRepository.findAll().stream()
                .map(category -> new CategoryAdminResponse(
                        category.getId(), category.getName(), category.getDescription(), category.isActive()))
                .toList();
    }

    @Transactional
    public CategoryAdminResponse createCategory(CategoryRequest request) {
        CargoCategory category = new CargoCategory();
        applyCategory(category, request);
        cargoCategoryRepository.save(category);
        return new CategoryAdminResponse(category.getId(), category.getName(),
                category.getDescription(), category.isActive());
    }

    @Transactional
    public CategoryAdminResponse updateCategory(Long id, CategoryRequest request) {
        CargoCategory category = cargoCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        applyCategory(category, request);
        cargoCategoryRepository.save(category);
        return new CategoryAdminResponse(category.getId(), category.getName(),
                category.getDescription(), category.isActive());
    }

    private void applyCategory(CargoCategory category, CategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(blankToNull(request.getDescription()));
        category.setActive(request.isActive());
    }

    // ---------- Ports ----------

    @Transactional(readOnly = true)
    public List<PortAdminResponse> listPorts() {
        return portRepository.findAll().stream()
                .map(port -> new PortAdminResponse(
                        port.getId(), port.getName(), port.getCode(), port.getCountry(),
                        port.getCity(), port.getLatitude(), port.getLongitude(), port.isActive()))
                .toList();
    }

    @Transactional
    public PortAdminResponse createPort(PortRequest request) {
        Port port = new Port();
        applyPort(port, request);
        portRepository.save(port);
        return toPortResponse(port);
    }

    @Transactional
    public PortAdminResponse updatePort(Long id, PortRequest request) {
        Port port = portRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port not found"));
        applyPort(port, request);
        portRepository.save(port);
        return toPortResponse(port);
    }

    private void applyPort(Port port, PortRequest request) {
        port.setName(request.getName().trim());
        port.setCode(request.getCode().trim().toUpperCase());
        port.setCountry(request.getCountry().trim());
        port.setCity(blankToNull(request.getCity()));
        port.setLatitude(request.getLatitude());
        port.setLongitude(request.getLongitude());
        port.setActive(request.isActive());
    }

    private PortAdminResponse toPortResponse(Port port) {
        return new PortAdminResponse(
                port.getId(), port.getName(), port.getCode(), port.getCountry(),
                port.getCity(), port.getLatitude(), port.getLongitude(), port.isActive());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
