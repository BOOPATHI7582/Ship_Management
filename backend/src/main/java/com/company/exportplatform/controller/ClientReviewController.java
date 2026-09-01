package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ReviewRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ReviewResponse;
import com.company.exportplatform.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/reviews")
@RequiredArgsConstructor
public class ClientReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewResponse> create(
            Authentication authentication,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok("Thank you for your feedback - your review is pending moderation",
                reviewService.create(authentication.getName(), request));
    }

    @GetMapping
    public ApiResponse<Page<ReviewResponse>> mine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(null, reviewService.listMine(authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    /** Convenience check so the UI knows whether a shipment was already reviewed. */
    @GetMapping("/exists")
    public ApiResponse<Boolean> exists(
            Authentication authentication,
            @RequestParam Long shipmentId) {
        return ApiResponse.ok(null, reviewService.hasReviewed(authentication.getName(), shipmentId));
    }
}
