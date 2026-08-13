package com.tickethub.booking.client;

import com.tickethub.booking.dto.ReserveTicketsRequest;
import com.tickethub.booking.dto.ReserveTicketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventory-service", url = "${app.services.inventory-url}")
public interface InventoryClient {
    @PostMapping("/inventory/reservations")
    List<ReserveTicketResponse> reserveTicket(@RequestBody ReserveTicketsRequest request);

    @PostMapping("/inventory/reservations/release")
    void releaseReservations(@RequestBody List<String> reservationIds);

    @PostMapping("/inventory/reservations/{reservationId}/confirm")
    public ReserveTicketResponse confirmReservation(@PathVariable String reservationId);

    @PostMapping("/inventory/reservations/{reservationId}/release")
    public ReserveTicketResponse releaseReservation(@PathVariable String reservationId);
}
