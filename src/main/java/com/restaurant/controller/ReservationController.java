package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.ReservationRequest;
import com.restaurant.model.Reservation;
import com.restaurant.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<List<Reservation>> getAll() {
        return ApiResponse.ok(reservationService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Reservation> getOne(@PathVariable Long id) {
        return ApiResponse.ok(reservationService.getById(id));
    }

    /** Reserves a table: checks capacity + time-slot overlap, auto-assigns a table if none is specified. */
    @PostMapping
    public ApiResponse<Reservation> create(@Valid @RequestBody ReservationRequest request) {
        return ApiResponse.ok("Reservation confirmed", reservationService.createReservation(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Reservation> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Reservation cancelled", reservationService.cancel(id));
    }

    @PostMapping("/{id}/seat")
    public ApiResponse<Reservation> seat(@PathVariable Long id) {
        return ApiResponse.ok("Guests seated", reservationService.seatGuests(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Reservation> complete(@PathVariable Long id) {
        return ApiResponse.ok("Reservation completed", reservationService.complete(id));
    }
}
