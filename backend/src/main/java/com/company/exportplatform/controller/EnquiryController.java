package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ExportEnquiryRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.EnquiryResponse;
import com.company.exportplatform.service.EnquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnquiryResponse>> create(
            Authentication authentication,
            @Valid @RequestBody ExportEnquiryRequest request) {
        EnquiryResponse response = enquiryService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Export requirement submitted. Reference: " + response.referenceNo(), response));
    }

    @GetMapping
    public ApiResponse<Page<EnquiryResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EnquiryResponse> result = enquiryService.listForClient(authentication.getName(),
                PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("My enquiries", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<EnquiryResponse> get(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Enquiry details", enquiryService.getOwned(authentication.getName(), id));
    }
}
