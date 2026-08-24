package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public Razorpay webhook receiver (under /api/public/** so it needs no auth).
 * Security comes from HMAC verification of the raw body against the webhook
 * secret; replay protection from the unique event_id log.
 */
@RestController
@RequestMapping("/api/public/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> webhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        String result = paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok(ApiResponse.ok(result, null));
    }
}
