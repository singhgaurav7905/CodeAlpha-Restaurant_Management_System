package com.restaurant.config;

import com.restaurant.model.Reservation;
import com.restaurant.model.ReservationStatus;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableStatus;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background housekeeping so the floor plan and reservation list stay
 * accurate without a staff member having to babysit them.
 */
@Component
public class ScheduledTasks {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    @Value("${restaurant.table.cleaning-minutes:15}")
    private int cleaningMinutes;

    @Value("${restaurant.reservation.no-show-grace-minutes:30}")
    private int noShowGraceMinutes;

    public ScheduledTasks(RestaurantTableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    /** Every 5 minutes: a table sitting in CLEANING past the grace period goes back to AVAILABLE on its own. */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void freeUpCleanedTables() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cleaningMinutes);
        List<RestaurantTable> cleaning = tableRepository.findByStatus(TableStatus.CLEANING);
        for (RestaurantTable table : cleaning) {
            if (table.getStatusUpdatedAt() != null && table.getStatusUpdatedAt().isBefore(cutoff)) {
                table.markStatus(TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
        }
    }

    /** Every 5 minutes: a reservation nobody showed up for gets marked NO_SHOW and its table is freed. */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void markNoShows() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> active = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
                .toList();

        for (Reservation reservation : active) {
            LocalDateTime graceEnd = reservation.getReservationTime().plusMinutes(noShowGraceMinutes);
            if (now.isAfter(graceEnd)) {
                reservation.setStatus(ReservationStatus.NO_SHOW);
                reservationRepository.save(reservation);

                RestaurantTable table = reservation.getTable();
                if (table.getStatus() == TableStatus.RESERVED) {
                    table.markStatus(TableStatus.AVAILABLE);
                    tableRepository.save(table);
                }
            }
        }
    }
}
