package com.tickethub.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReserveTicketRequest {

    @NotBlank(message = "bookingId is required")
    private String bookingId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "ticketTypeId is required")
    private String ticketTypeId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
