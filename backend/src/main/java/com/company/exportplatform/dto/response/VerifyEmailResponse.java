package com.company.exportplatform.dto.response;

public record VerifyEmailResponse(
        String email,
        boolean verified) {
}
