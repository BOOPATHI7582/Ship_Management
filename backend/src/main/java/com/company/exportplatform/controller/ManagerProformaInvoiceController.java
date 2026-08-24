package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ProformaRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ProformaResponse;
import com.company.exportplatform.service.ProformaInvoiceService;
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
@RequestMapping("/api/manager/proforma-invoices")
@RequiredArgsConstructor
public class ManagerProformaInvoiceController {

    private final ProformaInvoiceService proformaInvoiceService;

    @GetMapping
    public ApiResponse<Page<ProformaResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long quotationId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProformaResponse> result = proformaInvoiceService.list(status, quotationId, search,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Proforma invoices", result);
    }

    @PostMapping
    public ApiResponse<ProformaResponse> create(
            Authentication authentication,
            @Valid @RequestBody ProformaRequest request) {
        return ApiResponse.ok("Proforma invoice draft created",
                proformaInvoiceService.create(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProformaResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok("Proforma invoice", proformaInvoiceService.detailForManager(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProformaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProformaRequest request) {
        return ApiResponse.ok("Proforma invoice updated",
                proformaInvoiceService.update(id, request));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<ProformaResponse> send(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Proforma invoice sent to client",
                proformaInvoiceService.send(id, authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ProformaResponse> cancel(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Proforma invoice cancelled",
                proformaInvoiceService.cancel(id, authentication.getName()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        ProformaResponse pi = proformaInvoiceService.detailForManager(id);
        byte[] pdf = proformaInvoiceService.pdfForManager(id);
        String filename = (pi.piNo() != null ? pi.piNo() : "proforma-invoice") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
