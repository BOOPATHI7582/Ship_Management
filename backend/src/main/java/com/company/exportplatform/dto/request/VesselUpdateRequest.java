package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VesselUpdateRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 20)
    private String imoNumber;

    @Size(max = 50)
    private String vesselType;

    private BigDecimal capacity;

    @Size(max = 20)
    private String capacityUnit;

    @Size(max = 80)
    private String flag;

    @Size(max = 200)
    private String currentLocation;

    @Pattern(regexp = "AVAILABLE|LOADING|LOADING_COMPLETED|IN_TRANSIT|ARRIVED|MAINTENANCE",
            message = "Invalid vessel status")
    private String status;

    @Email
    @Size(max = 150)
    private String managementContact;

    @Size(max = 150)
    private String managementCompany;

    @Size(max = 2000)
    private String description;
}
