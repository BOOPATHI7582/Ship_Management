package com.company.exportplatform.dto.response;

import java.math.BigDecimal;
// import java.util.List;

public record VesselAdminResponse(
        Long id,
        String name,
        String imoNumber,
        String vesselType,
        BigDecimal capacity,
        String capacityUnit,
        String flag,
        String currentLocation,
        String status,
        String managementCompany,
        String managementContact,
        String description
) {
}
