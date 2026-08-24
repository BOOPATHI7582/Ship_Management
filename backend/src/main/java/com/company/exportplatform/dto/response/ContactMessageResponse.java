package com.company.exportplatform.dto.response;

import java.time.LocalDateTime;

/**
 * A public contact-form submission as seen by staff in the inbox,
 * or by the submitting client in their own "My Messages" list.
 */
public record ContactMessageResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String company,
        String subject,
        String message,
        boolean handled,
        String handledByName,
        LocalDateTime handledAt,
        LocalDateTime createdAt) {
}
