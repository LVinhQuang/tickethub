package com.tickethub.inventory.controller;

import com.tickethub.inventory.dto.CreateInventoryRequest;
import com.tickethub.inventory.dto.ReservationResponse;
import com.tickethub.inventory.dto.ReserveTicketRequest;
import com.tickethub.inventory.dto.TicketInventoryResponseDTO;
import com.tickethub.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<TicketInventoryResponseDTO> createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        TicketInventoryResponseDTO response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketTypeId}")
    public TicketInventoryResponseDTO getTicketInventory(@PathVariable String ticketTypeId) {
        return inventoryService.getTicketInventory(ticketTypeId);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserveTicket(
            @Valid @RequestBody ReserveTicketRequest request
    ) {
        ReservationResponse response = inventoryService.reserveTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirmReservation(@PathVariable String reservationId) {
        return inventoryService.confirmReservation(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/release")
    public ReservationResponse releaseReservation(@PathVariable String reservationId) {
        return inventoryService.releaseReservation(reservationId);
    }
}
