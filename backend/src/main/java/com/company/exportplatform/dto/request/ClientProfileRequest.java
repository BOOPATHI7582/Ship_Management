package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @Size(max = 200, message = "Company name must be at most 200 characters")
    private String companyName;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 80, message = "Country must be at most 80 characters")
    private String country;

    @Pattern(regexp = "(?i)^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]{3}$|^$",
            message = "GSTIN format is invalid")
    private String gstin;

    @Size(max = 255) private String addressLine1;
    @Size(max = 255) private String addressLine2;
    @Size(max = 100) private String city;
    @Size(max = 100) private String state;
    @Size(max = 20)  private String postalCode;
}
