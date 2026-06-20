package com.tickethub.catalog.service;

import com.tickethub.catalog.dto.*;
import com.tickethub.catalog.entity.Event;
import com.tickethub.catalog.entity.TicketType;
import com.tickethub.catalog.repository.EventRepository;
import com.tickethub.catalog.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getActiveEvents() {
        return eventRepository.findByStatus("ACTIVE").stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return mapToEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponseDTO> getTicketTypesByEventId(String eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found");
        }
        return ticketTypeRepository.findByEventId(eventId).stream()
                .map(this::mapToTicketTypeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketTypeResponseDTO getTicketTypeById(String id) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket type not found"));
        return mapToTicketTypeResponse(ticketType);
    }

    @Transactional
    public EventResponseDTO createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venue(request.getVenue())
                .startTime(request.getStartTime())
                .imageUrl(request.getImageUrl())
                .status("ACTIVE")
                .build();
        Event saved = eventRepository.save(event);
        return mapToEventResponse(saved);
    }

    @Transactional
    public EventResponseDTO updateEvent(String id, CreateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setStartTime(request.getStartTime());
        event.setImageUrl(request.getImageUrl());
        
        Event saved = eventRepository.save(event);
        return mapToEventResponse(saved);
    }

    @Transactional
    public TicketTypeResponseDTO addTicketType(String eventId, CreateTicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketType ticketType = TicketType.builder()
                .name(request.getName())
                .price(request.getPrice())
                .event(event)
                .build();
        
        TicketType saved = ticketTypeRepository.save(ticketType);
        return mapToTicketTypeResponse(saved);
    }

    private EventResponseDTO mapToEventResponse(Event event) {
        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startTime(event.getStartTime())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus())
                .build();
    }

    private TicketTypeResponseDTO mapToTicketTypeResponse(TicketType ticketType) {
        return TicketTypeResponseDTO.builder()
                .id(ticketType.getId())
                .name(ticketType.getName())
                .price(ticketType.getPrice())
                .build();
    }
}
