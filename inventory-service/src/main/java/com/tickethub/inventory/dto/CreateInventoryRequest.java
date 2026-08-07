package com.tickethub.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateInventoryRequest {

    @NotBlank(message = "ticketTypeId is required")
    private String ticketTypeId;

    @NotNull(message = "initialQuantity is required")
    @PositiveOrZero(message = "initialQuantity must be zero or greater")
    private Integer initialQuantity;
}
