package com.company.exportplatform.dto.response;

import java.math.BigDecimal;

public record PortAdminResponse(
        Long id,
        String name,
        String code,
        String country,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean active
) {
}
