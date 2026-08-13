package com.tickethub.inventory.controller;

import com.tickethub.inventory.dto.CreateInventoryRequest;
import com.tickethub.inventory.dto.ReserveTicketsRequest;
import com.tickethub.inventory.dto.TicketInventoryResponse;
import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.service.InventoryReservationService;
import com.tickethub.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryReservationService inventoryReservationService;

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
    public ResponseEntity<List<TicketReservationDTO>> reserveTickets(
            @Valid @RequestBody ReserveTicketsRequest request
    ) {
        List<TicketReservationDTO> response = inventoryService.reserveTickets(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reservations/release")
    public List<TicketReservationDTO> releaseReservations(
            @RequestBody
            @NotEmpty(message = "reservationIds must contain at least one reservation")
            List<@NotBlank(message = "reservationId is required") String> reservationIds
    ) {
        return inventoryService.releaseReservations(reservationIds);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public TicketReservationDTO confirmReservation(@PathVariable String reservationId) {
        return inventoryReservationService.confirmReservation(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/release")
    public TicketReservationDTO releaseReservation(@PathVariable String reservationId) {
        return inventoryReservationService.releaseReservation(reservationId);
    }
}
