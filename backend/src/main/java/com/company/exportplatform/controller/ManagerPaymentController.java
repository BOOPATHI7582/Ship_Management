package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.OfflinePaymentRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.PaymentResponse;
import com.company.exportplatform.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/manager/payments")
public class ManagerPaymentController {

    private final PaymentService paymentService;

    public ManagerPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(null,
                paymentService.listAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(null, paymentService.getForManager(id)));
    }

    /** Offline settlement (NEFT/RTGS/cheque etc.) recorded by staff - fully audited. */
    @PostMapping("/offline")
    public ResponseEntity<ApiResponse<PaymentResponse>> offline(
            Authentication authentication,
            @Valid @RequestBody OfflinePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Offline payment recorded",
                paymentService.recordOffline(authentication.getName(), request)));
    }
}
