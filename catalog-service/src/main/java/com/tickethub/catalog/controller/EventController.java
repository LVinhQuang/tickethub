package com.tickethub.catalog.controller;

import com.tickethub.catalog.dto.EventResponse;
import com.tickethub.catalog.dto.TicketTypeResponse;
import com.tickethub.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final CatalogService catalogService;

    @GetMapping
    public List<EventResponse> getEvents() {
        return catalogService.getActiveEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable String id) {
        return catalogService.getEventById(id);
    }

    @GetMapping("/{id}/ticket-types")
    public List<TicketTypeResponse> getTicketTypes(@PathVariable String id) {
        return catalogService.getTicketTypesByEventId(id);
    }

    @GetMapping("/ticket-types/{id}")
    public TicketTypeResponse getTicketTypeById(@PathVariable String id) {
        return catalogService.getTicketTypeById(id);
    }
}
