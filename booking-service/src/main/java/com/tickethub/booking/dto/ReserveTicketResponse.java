package com.tickethub.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveTicketResponse {

    private String id;
    private String bookingId;
    private String userId;
    private String ticketTypeId;
    private Integer quantity;
    private String status;
    private LocalDateTime expiresAt;
}