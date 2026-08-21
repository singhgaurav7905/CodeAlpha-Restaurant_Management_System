package com.restaurant.repository;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTable(RestaurantTable table);

    List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Reservation> findByTableAndStatusIn(RestaurantTable table, List<com.restaurant.model.ReservationStatus> statuses);
}
