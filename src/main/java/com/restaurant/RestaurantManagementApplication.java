package com.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Restaurant Management System.
 *
 * Boots an embedded server exposing REST APIs for menu, orders, tables,
 * reservations, inventory and reporting, plus serves the static
 * customer/admin web UI from src/main/resources/static.
 */
@SpringBootApplication
@EnableScheduling
public class RestaurantManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantManagementApplication.class, args);
    }
}
