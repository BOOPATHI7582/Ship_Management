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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/receipts")
@RequiredArgsConstructor
public class ManagerReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public ApiResponse<Page<ReceiptResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(null,
                receiptService.listAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReceiptResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(null, receiptService.getForManager(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        ReceiptResponse receipt = receiptService.getForManager(id);
        byte[] pdf = receiptService.pdfForManager(id);
        String filename = (receipt.receiptNo() != null ? receipt.receiptNo() : "receipt") + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
