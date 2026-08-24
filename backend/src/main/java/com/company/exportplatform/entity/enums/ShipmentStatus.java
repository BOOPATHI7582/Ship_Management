package com.company.exportplatform.entity.enums;

/**
 * Shipment lifecycle per spec - forward-only transitions.
 */
public enum ShipmentStatus {
    BOOKING_CONFIRMED,
    CARGO_PREPARATION,
    LOADING,
    LOADING_COMPLETED,
    DEPARTED,
    IN_TRANSIT,
    NEAR_DESTINATION,
    ARRIVED,
    UNLOADING,
    DELIVERED,
    COMPLETED
}
