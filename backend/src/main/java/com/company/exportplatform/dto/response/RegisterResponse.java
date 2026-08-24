package com.company.exportplatform.dto.response;

/**
 * Registration no longer signs the user in: email verification is required.
 * devVerificationUrl is only populated when mail delivery is disabled so
 * local development can still complete the flow.
 */
public record RegisterResponse(
        String email,
        boolean requiresVerification,
        String devVerificationUrl) {
}
