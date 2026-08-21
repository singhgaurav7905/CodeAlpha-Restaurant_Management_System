package com.restaurant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A physical dining table. Named RestaurantTable to avoid clashing with
 * the reserved SQL keyword / java.sql concept of "Table".
 */
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String tableNumber;

    @Min(1)
    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;

    /** e.g. "Patio", "Main Hall", "Rooftop" - lets the UI group tables. */
    private String location;

    /** When status last changed - lets scheduled jobs know how long a table has been CLEANING, etc. */
    private java.time.LocalDateTime statusUpdatedAt = java.time.LocalDateTime.now();

    public RestaurantTable() {}

    public RestaurantTable(String tableNumber, int capacity, String location) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }

    /** Use this instead of setStatus() when changing status - it also stamps when the change happened. */
    public void markStatus(TableStatus status) {
        this.status = status;
        this.statusUpdatedAt = java.time.LocalDateTime.now();
    }

    public java.time.LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(java.time.LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
