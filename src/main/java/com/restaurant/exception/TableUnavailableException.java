package com.restaurant.exception;

/** Thrown when a table cannot be booked/seated for the requested time or party size. */
public class TableUnavailableException extends RuntimeException {
    public TableUnavailableException(String message) {
        super(message);
    }
}
