package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ShipmentResponse;
import com.company.exportplatform.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/shipments")
@RequiredArgsConstructor
public class ClientShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ApiResponse<Page<ShipmentResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Your shipments", shipmentService.listMine(
                authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> detail(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Shipment",
                shipmentService.detailForClient(authentication.getName(), id));
    }
}
