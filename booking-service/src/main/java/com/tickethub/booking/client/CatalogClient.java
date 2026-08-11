package com.tickethub.booking.client;

import com.tickethub.booking.dto.TicketTypeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${app.services.catalog-url}")
public interface CatalogClient {

    @GetMapping("/events/ticket-types/{id}")
    TicketTypeResponse getTicketTypeById(@PathVariable("id") String id);
}
