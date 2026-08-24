package com.company.exportplatform.dto.response;

public record PublicPortResponse(
        Long id,
        String name,
        String code,
        String country,
        String city
) {
}
