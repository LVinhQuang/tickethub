package com.tickethub.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReserveTicketsRequest {

    @NotBlank(message = "bookingId is required")
    private String bookingId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotEmpty(message = "items must contain at least one ticket")
    @Valid
    private List<@NotNull(message = "item is required") Item> items;

    @Data
    public static class Item {

        @NotBlank(message = "ticketTypeId is required")
        private String ticketTypeId;

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        private Integer quantity;
    }
}
