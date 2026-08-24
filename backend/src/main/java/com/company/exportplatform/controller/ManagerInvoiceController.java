package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.InvoiceRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.InvoiceResponse;
import com.company.exportplatform.service.InvoiceService;
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
@RequestMapping("/api/manager/invoices")
@RequiredArgsConstructor
public class ManagerInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<Page<InvoiceResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceResponse> result = invoiceService.list(status, search,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Tax invoices", result);
    }

    @PostMapping
    public ApiResponse<InvoiceResponse> issue(
            Authentication authentication,
            @Valid @RequestBody InvoiceRequest request) {
        return ApiResponse.ok("Tax invoice issued",
                invoiceService.issue(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok("Tax invoice", invoiceService.detailForManager(id));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<InvoiceResponse> send(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Tax invoice sent to client",
                invoiceService.send(id, authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<InvoiceResponse> cancel(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Tax invoice cancelled",
                invoiceService.cancel(id, authentication.getName()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        InvoiceResponse inv = invoiceService.detailForManager(id);
        byte[] pdf = invoiceService.pdfForManager(id);
        String filename = (inv.invoiceNo() != null ? inv.invoiceNo() : "tax-invoice") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
