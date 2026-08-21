package com.restaurant.model;

/** Lifecycle of a customer order. */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    READY,
    SERVED,
    COMPLETED,
    CANCELLED
}
