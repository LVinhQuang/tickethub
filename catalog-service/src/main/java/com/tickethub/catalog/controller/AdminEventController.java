package com.tickethub.catalog.controller;

import com.tickethub.catalog.dto.*;
import com.tickethub.catalog.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final CatalogService catalogService;

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return catalogService.getAllEvents();
    }

    @PostMapping
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return catalogService.createEvent(request);
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable String id, @Valid @RequestBody CreateEventRequest request) {
        return catalogService.updateEvent(id, request);
    }

    @PostMapping("/{id}/ticket-types")
    public TicketTypeResponse addTicketType(
            @PathVariable String id,
            @Valid @RequestBody CreateTicketTypeRequest request
    ) {
        return catalogService.addTicketType(id, request);
    }
}
