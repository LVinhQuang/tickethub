package com.tickethub.booking.entity;

public enum BookingStatus {
    PENDING,
    WAITING_FOR_PAYMENT,
    PAYMENT_PROCESSING,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    FAILED
}
