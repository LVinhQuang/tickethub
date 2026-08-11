package com.tickethub.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketInventoryResponse {

    private String ticketTypeId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
}
