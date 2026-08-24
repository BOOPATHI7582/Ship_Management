package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.CargoRequest;
import com.company.exportplatform.dto.request.CategoryRequest;
import com.company.exportplatform.dto.request.PortRequest;
import com.company.exportplatform.dto.request.VesselRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.CargoAdminResponse;
import com.company.exportplatform.dto.response.CategoryAdminResponse;
import com.company.exportplatform.dto.response.PortAdminResponse;
import com.company.exportplatform.dto.response.VesselAdminResponse;
import com.company.exportplatform.service.AdminCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService catalogService;

    // ---------- Vessels ----------

    @GetMapping("/vessels")
    public ApiResponse<Page<VesselAdminResponse>> vessels(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Vessels", catalogService.listVessels(status,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/vessels")
    public ApiResponse<VesselAdminResponse> createVessel(@Valid @RequestBody VesselRequest request) {
        return ApiResponse.ok("Vessel created", catalogService.createVessel(request));
    }

    @PutMapping("/vessels/{id}")
    public ApiResponse<VesselAdminResponse> updateVessel(
            @PathVariable Long id, @Valid @RequestBody VesselRequest request) {
        return ApiResponse.ok("Vessel updated", catalogService.updateVessel(id, request));
    }

    // ---------- Cargo lots ----------

    @GetMapping("/cargo")
    public ApiResponse<Page<CargoAdminResponse>> cargo(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Cargo lots", catalogService.listCargo(status,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/cargo")
    public ApiResponse<CargoAdminResponse> createCargo(@Valid @RequestBody CargoRequest request) {
        return ApiResponse.ok("Cargo lot created", catalogService.createCargo(request));
    }

    @PutMapping("/cargo/{id}")
    public ApiResponse<CargoAdminResponse> updateCargo(
            @PathVariable Long id, @Valid @RequestBody CargoRequest request) {
        return ApiResponse.ok("Cargo lot updated", catalogService.updateCargo(id, request));
    }

    // ---------- Categories ----------

    @GetMapping("/categories")
    public ApiResponse<List<CategoryAdminResponse>> categories() {
        return ApiResponse.ok("Categories", catalogService.listCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryAdminResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("Category created", catalogService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryAdminResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("Category updated", catalogService.updateCategory(id, request));
    }

    // ---------- Ports ----------

    @GetMapping("/ports")
    public ApiResponse<List<PortAdminResponse>> ports() {
        return ApiResponse.ok("Ports", catalogService.listPorts());
    }

    @PostMapping("/ports")
    public ApiResponse<PortAdminResponse> createPort(@Valid @RequestBody PortRequest request) {
        return ApiResponse.ok("Port created", catalogService.createPort(request));
    }

    @PutMapping("/ports/{id}")
    public ApiResponse<PortAdminResponse> updatePort(
            @PathVariable Long id, @Valid @RequestBody PortRequest request) {
        return ApiResponse.ok("Port updated", catalogService.updatePort(id, request));
    }
}
