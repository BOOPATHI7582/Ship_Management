package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PortRequest {

    @NotBlank(message = "Port name is required")
    @Size(max = 120)
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 80)
    private String country;

    @Size(max = 80)
    private String city;

    @NotBlank(message = "Port code is required")
    @Size(max = 10, message = "Port code must be at most 10 characters")
    private String code;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private boolean active = true;
}
