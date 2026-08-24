package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ShipmentProgressRequest;
import com.company.exportplatform.dto.request.ShipmentRequest;
import com.company.exportplatform.dto.request.ShipmentUpdateRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ShipmentResponse;
import com.company.exportplatform.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/shipments")
@RequiredArgsConstructor
public class ManagerShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ApiResponse<Page<ShipmentResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(null, shipmentService.list(status, search,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping
    public ApiResponse<ShipmentResponse> create(
            Authentication authentication,
            @Valid @RequestBody ShipmentRequest request) {
        return ApiResponse.ok("Shipment booked", shipmentService.create(
                authentication.getName(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(null, shipmentService.detailForManager(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShipmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentUpdateRequest request) {
        return ApiResponse.ok("Shipment updated", shipmentService.update(id, request));
    }

    @PostMapping("/{id}/progress")
    public ApiResponse<ShipmentResponse> progress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ShipmentProgressRequest request) {
        return ApiResponse.ok("Tracking updated", shipmentService.progress(
                id, authentication.getName(), request));
    }
}
