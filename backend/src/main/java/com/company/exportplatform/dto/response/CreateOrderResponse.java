package com.company.exportplatform.dto.response;

import java.math.BigDecimal;


/**
 * Payload returned to the frontend to launch checkout. In MOCK mode (no gateway
 * keys configured) the response additionally carries a pre-signed mock payment
 * id + signature so the dev UI can exercise the exact verify path used by the
 * real Razorpay Checkout.
 */
public record CreateOrderResponse(
        Long paymentId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        String razorpayKeyId,
        String mockPaymentId,
        String mockSignature
) {
}
