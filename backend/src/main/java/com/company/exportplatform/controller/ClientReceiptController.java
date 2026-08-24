package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ReceiptResponse;
import com.company.exportplatform.service.ReceiptService;
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
@RequestMapping("/api/client/receipts")
@RequiredArgsConstructor
public class ClientReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public ApiResponse<Page<ReceiptResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Your payment receipts", receiptService.listMine(
                authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReceiptResponse> detail(
            Authentication authentication,
            @PathVariable Long id) {
        return ApiResponse.ok("Payment receipt",
                receiptService.detailForClient(authentication.getName(), id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(
            Authentication authentication,
            @PathVariable Long id) {
        ReceiptResponse receipt = receiptService.detailForClient(authentication.getName(), id);
        byte[] pdf = receiptService.pdfForClient(authentication.getName(), id);
        String filename = (receipt.receiptNo() != null ? receipt.receiptNo() : "receipt") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
