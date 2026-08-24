package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OfferRequest {

    @Positive(message = "Offer price must be positive")
    private BigDecimal offerPrice;

    @Size(max = 1000)
    private String message;
}
