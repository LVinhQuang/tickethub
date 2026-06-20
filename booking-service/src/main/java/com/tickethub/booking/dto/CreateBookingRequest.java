package com.tickethub.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateBookingRequest {
    @NotEmpty(message = "Booking items list cannot be empty")
    @Valid
    private List<BookingItemRequest> items;
}
