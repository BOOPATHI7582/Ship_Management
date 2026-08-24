package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnquiryStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "REVIEWING|CONTACTED|REJECTED|CLOSED", message = "Invalid enquiry status")
    private String status;
}
