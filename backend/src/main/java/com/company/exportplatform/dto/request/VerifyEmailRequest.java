package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification code is required")
        String token,

        @Email(message = "A valid email is required to enforce wrong-attempt limits")
        String email) {
}