package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.InvoiceResponse;
import com.company.exportplatform.service.InvoiceService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/invoices")
@RequiredArgsConstructor
public class ClientInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<Page<InvoiceResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceResponse> result = invoiceService.listMine(authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Your tax invoices", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> detail(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Tax invoice",
                invoiceService.detailForClient(authentication.getName(), id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(
            Authentication authentication,
            @PathVariable Long id) {
        InvoiceResponse inv = invoiceService.detailForClient(authentication.getName(), id);
        byte[] pdf = invoiceService.pdfForClient(authentication.getName(), id);
        String filename = (inv.invoiceNo() != null ? inv.invoiceNo() : "tax-invoice") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
