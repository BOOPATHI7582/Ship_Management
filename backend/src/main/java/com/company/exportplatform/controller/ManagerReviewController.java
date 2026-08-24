package com.company.exportplatform.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/reviews")
@RequiredArgsConstructor
public class ManagerReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ApiResponse<Page<ReviewResponse>> list(
            @RequestParam(defaultValue = "ALL") String moderation,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(null, reviewService.list(moderation, search,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/pending-count")
    public ApiResponse<Long> pendingCount() {
        return ApiResponse.ok(null, reviewService.countPending());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ReviewResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        return ApiResponse.ok("Review published", reviewService.moderate(id, true, authentication.getName()));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ReviewResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        return ApiResponse.ok("Review rejected", reviewService.moderate(id, false, authentication.getName()));
    }
}
