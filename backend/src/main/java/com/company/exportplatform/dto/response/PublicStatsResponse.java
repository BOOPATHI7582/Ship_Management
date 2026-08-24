package com.company.exportplatform.dto.response;

public record PublicStatsResponse(
        long activeVessels,
        long portsServed,
        long cargoLotsAvailable,
        long shipmentsDelivered
) {
}
