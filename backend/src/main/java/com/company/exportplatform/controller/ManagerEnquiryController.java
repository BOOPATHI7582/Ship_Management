package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.EnquiryStatusRequest;
import com.company.exportplatform.dto.request.OfferRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ManagerEnquiryResponse;
import com.company.exportplatform.dto.response.NegotiationResponse;
import com.company.exportplatform.service.ManagerEnquiryService;
import com.company.exportplatform.service.NegotiationService;
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
@RequestMapping("/api/manager/enquiries")
@RequiredArgsConstructor
public class ManagerEnquiryController {

    private final ManagerEnquiryService enquiryService;
    private final NegotiationService negotiationService;

    @GetMapping
    public ApiResponse<Page<ManagerEnquiryResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ManagerEnquiryResponse> result = enquiryService.list(status, search,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Enquiries", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ManagerEnquiryResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok("Enquiry detail", enquiryService.detail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ManagerEnquiryResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody EnquiryStatusRequest request) {
        return ApiResponse.ok("Enquiry status updated", enquiryService.updateStatus(id, request));
    }

    @GetMapping("/{id}/negotiation")
    public ApiResponse<NegotiationResponse> negotiation(@PathVariable Long id) {
        return ApiResponse.ok("Negotiation thread", negotiationService.forManager(id));
    }

    @PostMapping("/{id}/offers")
    public ApiResponse<NegotiationResponse> sendOffer(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OfferRequest request) {
        return ApiResponse.ok("Offer sent to client",
                negotiationService.sendOffer(id, authentication.getName(), request));
    }
}
