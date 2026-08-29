package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.CatalogOptionResponse;
import com.company.exportplatform.repository.CargoRepository;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.PortRepository;
import com.company.exportplatform.repository.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only reference data for staff forms (vessels, cargo lots, ports,
 * clients) - mirrors the admin catalog but is reachable by ship managers.
 */
@RestController
@RequestMapping("/api/manager/catalog")
@RequiredArgsConstructor
public class ManagerCatalogController {

    private final VesselRepository vesselRepository;
    private final CargoRepository cargoRepository;
    private final PortRepository portRepository;
    private final ClientRepository clientRepository;

    @GetMapping("/vessels")
    public ApiResponse<List<CatalogOptionResponse>> vessels() {
        return ApiResponse.ok(null, vesselRepository.findAll().stream()
                .map(v -> new CatalogOptionResponse(v.getId(), v.getName(),
                        v.getImoNumber() != null ? "IMO " + v.getImoNumber() : null))
                .toList());
    }

    @GetMapping("/cargo")
    public ApiResponse<List<CatalogOptionResponse>> cargo() {
        return ApiResponse.ok(null, cargoRepository.findAll().stream()
                .map(c -> new CatalogOptionResponse(c.getId(), c.getName(),
                        c.getQuantity() != null ? c.getQuantity().stripTrailingZeros().toPlainString()
                                + (c.getUnit() != null ? " " + c.getUnit() : "") : null))
                .toList());
    }

    @GetMapping("/ports")
    public ApiResponse<List<CatalogOptionResponse>> ports() {
        return ApiResponse.ok(null, portRepository.findAll().stream()
                .map(p -> new CatalogOptionResponse(p.getId(), p.getName(), p.getCode()))
                .toList());
    }

    @GetMapping("/clients")
    public ApiResponse<List<CatalogOptionResponse>> clients() {
        return ApiResponse.ok(null, clientRepository.findAllWithUser().stream()
                .map(c -> new CatalogOptionResponse(c.getId(), displayName(c.getUser()), c.getUser().getEmail()))
                .toList());
    }

    private static String displayName(com.company.exportplatform.entity.User user) {
        if (user == null) {
            return "Client";
        }
        return user.getCompanyName() != null && !user.getCompanyName().isBlank()
                ? user.getCompanyName() : user.getFullName();
    }
}
