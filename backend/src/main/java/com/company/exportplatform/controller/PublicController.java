package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.PublicCargoResponse;
import com.company.exportplatform.dto.response.PublicCategoryResponse;
import com.company.exportplatform.dto.response.PublicPortResponse;
import com.company.exportplatform.dto.response.PublicReviewResponse;
import com.company.exportplatform.dto.response.PublicStatsResponse;
import com.company.exportplatform.dto.response.PublicTrackingResponse;
import com.company.exportplatform.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Intentionally public, non-sensitive website data.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/cargo")
    public ApiResponse<List<PublicCargoResponse>> availableCargo() {
        return ApiResponse.ok("Available cargo lots", publicService.availableCargo());
    }

    @GetMapping("/cargo-categories")
    public ApiResponse<List<PublicCategoryResponse>> categories() {
        return ApiResponse.ok("Cargo categories", publicService.activeCategories());
    }

    @GetMapping("/ports")
    public ApiResponse<List<PublicPortResponse>> ports() {
        return ApiResponse.ok("Ports", publicService.activePorts());
    }

    @GetMapping("/tracking/{shipmentRef}")
    public ApiResponse<PublicTrackingResponse> tracking(@PathVariable String shipmentRef) {
        return ApiResponse.ok("Shipment tracking", publicService.trackByRef(shipmentRef));
    }

    @GetMapping("/stats")
    public ApiResponse<PublicStatsResponse> stats() {
        return ApiResponse.ok("Platform statistics", publicService.stats());
    }

    @GetMapping("/reviews")
    public ApiResponse<List<PublicReviewResponse>> reviews() {
        return ApiResponse.ok("Client reviews", publicService.approvedReviews());
    }
}
