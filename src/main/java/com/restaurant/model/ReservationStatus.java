package com.restaurant.model;

/** Lifecycle of a table reservation. */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    SEATED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
