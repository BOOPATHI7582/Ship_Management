package com.company.exportplatform.dto.request;

// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ClientReplyRequest {

    @Size(max = 1000)
    private String message;

    @Positive(message = "Counter price must be positive")
    private BigDecimal counterPrice;
}
