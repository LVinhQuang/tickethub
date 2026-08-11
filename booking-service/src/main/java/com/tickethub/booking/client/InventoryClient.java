package com.tickethub.booking.client;

import com.tickethub.booking.dto.ReserveTicketRequest;
import com.tickethub.booking.dto.ReserveTicketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${app.services.inventory-url}")
public interface InventoryClient {
    @PostMapping("/inventory/reservations")
    ReserveTicketResponse reserveTicket(@RequestBody ReserveTicketRequest request);
}
