package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull Long invoiceId
) {
}
