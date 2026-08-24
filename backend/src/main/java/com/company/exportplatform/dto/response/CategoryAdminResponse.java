package com.company.exportplatform.dto.response;

public record CategoryAdminResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}
