package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.QuotationDecisionRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ManagerQuotationSummary;
import com.company.exportplatform.dto.response.QuotationResponse;
import com.company.exportplatform.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/client/quotations")
@RequiredArgsConstructor
public class ClientQuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ApiResponse<Page<ManagerQuotationSummary>> mine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ManagerQuotationSummary> result = quotationService.listMine(authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Quotations", result);
    }

    @GetMapping("/enquiry/{enquiryId}")
    public ApiResponse<java.util.List<ManagerQuotationSummary>> forEnquiry(
            Authentication authentication,
            @PathVariable Long enquiryId) {
        return ApiResponse.ok("Quotations",
                quotationService.listMineForEnquiry(authentication.getName(), enquiryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuotationResponse> detail(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Quotation",
                quotationService.viewAsClient(authentication.getName(), id));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<QuotationResponse> accept(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody QuotationDecisionRequest request) {
        QuotationDecisionRequest accept = new QuotationDecisionRequest("ACCEPT", request.reason());
        return ApiResponse.ok("Quotation accepted",
                quotationService.respond(authentication.getName(), id, accept));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<QuotationResponse> reject(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody QuotationDecisionRequest request) {
        QuotationDecisionRequest reject = new QuotationDecisionRequest("REJECT", request.reason());
        return ApiResponse.ok("Quotation declined",
                quotationService.respond(authentication.getName(), id, reject));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(
            Authentication authentication,
            @PathVariable Long id) {
        String quoteNo = quotationService.viewAsClient(authentication.getName(), id).quoteNo();
        byte[] pdf = quotationService.pdfForClient(authentication.getName(), id);
        String filename = (quoteNo != null ? quoteNo : "quotation") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
