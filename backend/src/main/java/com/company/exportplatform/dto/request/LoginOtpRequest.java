package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginOtpRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Verification code is required") String otp
) {
}