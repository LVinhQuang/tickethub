package com.tickethub.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReserveTicketsRequest {

    @NotBlank(message = "bookingId is required")
    private String bookingId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotEmpty(message = "items must contain at least one ticket")
    @Valid
    private List<@NotNull BookingItemRequest> items;
}