package com.restaurant.exception;

/** Thrown when an order would consume more of an ingredient than is currently in stock. */
public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
