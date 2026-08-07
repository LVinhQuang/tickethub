package com.tickethub.inventory.dto;

import com.tickethub.inventory.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private String id;
    private String bookingId;
    private String userId;
    private String ticketTypeId;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime expiresAt;
}
