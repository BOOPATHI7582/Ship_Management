package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification code is required")
        String token,

        String email) {
}