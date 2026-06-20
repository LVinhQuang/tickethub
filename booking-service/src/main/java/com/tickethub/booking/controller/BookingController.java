package com.tickethub.booking.controller;

import com.tickethub.booking.dto.BookingResponseDTO;
import com.tickethub.booking.dto.CreateBookingRequest;
import com.tickethub.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDTO createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(userId, request);
    }

    @GetMapping
    public List<BookingResponseDTO> getMyBookings(@RequestHeader("X-User-Id") String userId) {
        return bookingService.getBookingsByUserId(userId);
    }

    @GetMapping("/{id}")
    public BookingResponseDTO getBookingById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        return bookingService.getBookingById(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponseDTO cancelBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        return bookingService.cancelBooking(userId, id);
    }
}
