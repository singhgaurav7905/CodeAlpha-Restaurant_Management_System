package com.restaurant.model;

/**
 * Staff account permission level. Both roles can use the admin dashboard
 * today; kept as two roles so you can lock down sensitive actions to
 * ADMIN only later (e.g. via @PreAuthorize("hasRole('ADMIN')")).
 */
public enum Role {
    ADMIN,
    STAFF
}
