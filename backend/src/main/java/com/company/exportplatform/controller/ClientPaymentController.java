package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.CreateOrderRequest;
import com.company.exportplatform.dto.request.VerifyPaymentRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.CreateOrderResponse;
import com.company.exportplatform.dto.response.PaymentResponse;
import com.company.exportplatform.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/payments")
public class ClientPaymentController {

    private final PaymentService paymentService;

    public ClientPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order created",
                paymentService.createOrder(authentication.getName(), request)));
    }

    /** Handshake called by the frontend after Razorpay Checkout (or the dev mock panel). */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verify(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment verified",
                paymentService.verify(authentication.getName(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> mine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(null, paymentService.listMine(
                authentication.getName(),
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")))));
    }
}
