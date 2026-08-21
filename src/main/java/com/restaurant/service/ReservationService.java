package com.restaurant.service;

import com.restaurant.dto.ReservationRequest;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.exception.TableUnavailableException;
import com.restaurant.model.*;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Handles reservation creation, cancellation and seating, including the
 * core "table availability" logic: a table is free for a requested slot
 * only if no other active reservation for that table overlaps the window.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;

    @Value("${restaurant.reservation.slot-minutes:90}")
    private int defaultSlotMinutes;

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.SEATED);

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, RestaurantTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    /**
     * True if the given table has no active reservation whose window
     * [start, start+duration) overlaps the requested [start, end) window.
     */
    public boolean isTableFreeForSlot(RestaurantTable table, LocalDateTime start, LocalDateTime end) {
        List<Reservation> existing = reservationRepository.findByTableAndStatusIn(table, ACTIVE_STATUSES);
        for (Reservation r : existing) {
            LocalDateTime rStart = r.getReservationTime();
            LocalDateTime rEnd = r.getEndTime();
            boolean overlaps = start.isBefore(rEnd) && rStart.isBefore(end);
            if (overlaps) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : defaultSlotMinutes;
        LocalDateTime start = request.getReservationTime();
        LocalDateTime end = start.plusMinutes(duration);

        RestaurantTable table;
        if (request.getTableId() != null) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.getTableId()));
            if (table.getCapacity() < request.getPartySize()) {
                throw new TableUnavailableException("Table " + table.getTableNumber() + " seats " + table.getCapacity()
                        + " but party size is " + request.getPartySize());
            }
            if (!isTableFreeForSlot(table, start, end)) {
                throw new TableUnavailableException("Table " + table.getTableNumber() + " is already booked for that time");
            }
        } else {
            // Auto-assign: smallest table that fits the party and is free for the slot.
            table = tableRepository.findAll().stream()
                    .filter(t -> t.getCapacity() >= request.getPartySize())
                    .filter(t -> isTableFreeForSlot(t, start, end))
                    .min(Comparator.comparingInt(RestaurantTable::getCapacity))
                    .orElseThrow(() -> new TableUnavailableException(
                            "No table available for a party of " + request.getPartySize() + " at " + start));
        }

        Reservation reservation = new Reservation();
        reservation.setTable(table);
        reservation.setGuestName(request.getGuestName());
        reservation.setGuestPhone(request.getGuestPhone());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationTime(start);
        reservation.setDurationMinutes(duration);
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // If the slot starts now (or very soon), reflect it on the live table map immediately.
        if (!start.isAfter(LocalDateTime.now().plusMinutes(15))) {
            table.markStatus(TableStatus.RESERVED);
            tableRepository.save(table);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancel(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.CANCELLED);
        RestaurantTable table = reservation.getTable();
        if (table.getStatus() == TableStatus.RESERVED) {
            table.markStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation seatGuests(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.SEATED);
        RestaurantTable table = reservation.getTable();
        table.markStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation complete(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.COMPLETED);
        RestaurantTable table = reservation.getTable();
        table.markStatus(TableStatus.CLEANING);
        tableRepository.save(table);
        return reservationRepository.save(reservation);
    }
}
