package com.tickethub.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemResponseDTO {
    private String id;
    private String ticketTypeId;
    private Integer quantity;
    private BigDecimal price;
}
