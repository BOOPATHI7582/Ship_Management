package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.QuotationResponse;
import com.company.exportplatform.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Secure-token access: /quotation/:secureToken hides internal ids and lets
 * guests review a quotation without an account. Viewing marks it VIEWED once.
 */
@RestController
@RequestMapping("/api/public/quotations")
@RequiredArgsConstructor
public class PublicQuotationController {

    private final QuotationService quotationService;

    @GetMapping("/{secureToken}")
    public ApiResponse<QuotationResponse> byToken(@PathVariable String secureToken) {
        return ApiResponse.ok("Quotation", quotationService.viewByToken(secureToken));
    }

    @GetMapping("/{secureToken}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String secureToken) {
        byte[] pdf = quotationService.pdfByToken(secureToken);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"quotation.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
