package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ClientReplyRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.NegotiationResponse;
import com.company.exportplatform.service.NegotiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/negotiations")
@RequiredArgsConstructor
public class ClientNegotiationController {

    private final NegotiationService negotiationService;

    @GetMapping("/enquiry/{enquiryId}")
    public ApiResponse<NegotiationResponse> get(Authentication authentication, @PathVariable Long enquiryId) {
        return ApiResponse.ok("Negotiation thread",
                negotiationService.forClient(authentication.getName(), enquiryId));
    }

    @PostMapping("/enquiry/{enquiryId}/messages")
    public ApiResponse<NegotiationResponse> reply(
            Authentication authentication,
            @PathVariable Long enquiryId,
            @Valid @RequestBody ClientReplyRequest request) {
        NegotiationService.NegotiationReplyResult result =
                negotiationService.replyFromClient(authentication.getName(), enquiryId, request);
        String msg = request.getCounterPrice() != null ? "Counter offer sent" : "Message sent";
        return ApiResponse.ok(msg, result.thread());
    }

    @PostMapping("/messages/{messageId}/accept")
    public ApiResponse<NegotiationResponse> accept(
            Authentication authentication, @PathVariable Long messageId) {
        NegotiationResponse response = negotiationService.accept(authentication.getName(), messageId);
        return ApiResponse.ok("Offer accepted. Our team will prepare the quotation.", response);
    }

    @PostMapping("/messages/{messageId}/reject")
    public ApiResponse<NegotiationResponse> reject(
            Authentication authentication, @PathVariable Long messageId) {
        return ApiResponse.ok("Offer declined",
                negotiationService.reject(authentication.getName(), messageId));
    }
}
