package com.tickethub.inventory.controller;

import com.tickethub.inventory.dto.CreateInventoryRequest;
import com.tickethub.inventory.dto.TicketInventoryResponse;
import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.dto.ReserveTicketRequest;
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
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<TicketInventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        TicketInventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketTypeId}")
    public TicketInventoryResponse getTicketInventory(@PathVariable String ticketTypeId) {
        return inventoryService.getTicketInventory(ticketTypeId);
    }

    @PostMapping("/reservations")
    public ResponseEntity<TicketReservationDTO> reserveTicket(
            @Valid @RequestBody ReserveTicketRequest request
    ) {
        TicketReservationDTO response = inventoryService.reserveTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public TicketReservationDTO confirmReservation(@PathVariable String reservationId) {
        return inventoryService.confirmReservation(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/release")
    public TicketReservationDTO releaseReservation(@PathVariable String reservationId) {
        return inventoryService.releaseReservation(reservationId);
    }
}
