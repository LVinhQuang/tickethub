package com.tickethub.booking.controller;

import com.tickethub.booking.dto.BookingResponse;
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
    public BookingResponse createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(userId, request);
    }

    @GetMapping
    public List<BookingResponse> getMyBookings(@RequestHeader("X-User-Id") String userId) {
        return bookingService.getBookingsByUserId(userId);
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        return bookingService.getBookingById(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        return bookingService.cancelBooking(userId, id);
    }
}
