package com.tickethub.catalog.controller;

import com.tickethub.catalog.dto.EventResponseDTO;
import com.tickethub.catalog.dto.TicketTypeResponseDTO;
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
    public List<EventResponseDTO> getEvents() {
        return catalogService.getActiveEvents();
    }

    @GetMapping("/{id}")
    public EventResponseDTO getEventById(@PathVariable String id) {
        return catalogService.getEventById(id);
    }

    @GetMapping("/{id}/ticket-types")
    public List<TicketTypeResponseDTO> getTicketTypes(@PathVariable String id) {
        return catalogService.getTicketTypesByEventId(id);
    }

    @GetMapping("/ticket-types/{id}")
    public TicketTypeResponseDTO getTicketTypeById(@PathVariable String id) {
        return catalogService.getTicketTypeById(id);
    }
}
