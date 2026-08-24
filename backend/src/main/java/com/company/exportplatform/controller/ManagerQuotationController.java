package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.QuotationRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/quotations")
@RequiredArgsConstructor
public class ManagerQuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ApiResponse<Page<ManagerQuotationSummary>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long enquiryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ManagerQuotationSummary> result = quotationService.list(status, enquiryId,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Quotations", result);
    }

    @PostMapping
    public ApiResponse<QuotationResponse> create(
            Authentication authentication,
            @Valid @RequestBody QuotationRequest request) {
        return ApiResponse.ok("Quotation draft created",
                quotationService.create(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuotationResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok("Quotation", quotationService.detailForManager(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuotationResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody QuotationRequest request) {
        return ApiResponse.ok("Quotation updated",
                quotationService.update(id, authentication.getName(), request));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<QuotationResponse> send(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Quotation sent to client",
                quotationService.send(id, authentication.getName()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        String quoteNo = quotationService.detailForManager(id).quoteNo();
        byte[] pdf = quotationService.pdfForManager(id);
        return pdfEntity(quoteNo, pdf);
    }

    private ResponseEntity<byte[]> pdfEntity(String quoteNo, byte[] pdf) {
        String filename = (quoteNo != null ? quoteNo : "quotation") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
