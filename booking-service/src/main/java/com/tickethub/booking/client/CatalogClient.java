package com.tickethub.booking.client;

import com.tickethub.booking.dto.TicketTypeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "http://localhost:8082")
public interface CatalogClient {

    @GetMapping("/events/ticket-types/{id}")
    TicketTypeResponseDTO getTicketTypeById(@PathVariable("id") String id);
}
