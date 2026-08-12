package com.tickethub.inventory.service;

import com.tickethub.inventory.dto.CreateInventoryRequest;
import com.tickethub.inventory.dto.ReserveTicketsRequest;
import com.tickethub.inventory.dto.TicketInventoryResponse;
import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.entity.TicketInventory;
import com.tickethub.inventory.mapper.InventoryMapper;
import com.tickethub.inventory.repository.TicketInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final long RESERVATION_TIMEOUT_MINUTES = 15;

    private final TicketInventoryRepository ticketInventoryRepository;
    private final InventoryReservationService inventoryReservationService;
    private final InventoryMapper inventoryMapper;

    public TicketInventoryResponse createInventory(CreateInventoryRequest request) {
        if (ticketInventoryRepository.findByTicketTypeId(request.getTicketTypeId()).isPresent()) {
            throw new IllegalStateException(
                    "Inventory already exists for ticket type: " + request.getTicketTypeId()
            );
        }

        TicketInventory inventory = TicketInventory.builder()
                .ticketTypeId(request.getTicketTypeId())
                .availableQuantity(request.getInitialQuantity())
                .reservedQuantity(0)
                .soldQuantity(0)
                .build();

        TicketInventory savedInventory = ticketInventoryRepository.save(inventory);

        return inventoryMapper.toTicketInventoryResponseDTO(savedInventory);
    }

    public TicketInventoryResponse getTicketInventory(String ticketTypeId) {
        TicketInventory inventory = ticketInventoryRepository.findByTicketTypeId(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory not found for ticket type: " + ticketTypeId
                ));

        return inventoryMapper.toTicketInventoryResponseDTO(inventory);
    }

    @Transactional
    public List<TicketReservationDTO> reserveTickets(ReserveTicketsRequest request) {
        List<ReserveTicketsRequest.Item> items = request.getItems().stream()
                .sorted(Comparator.comparing(ReserveTicketsRequest.Item::getTicketTypeId))
                .toList();

        validateUniqueTicketTypes(items);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(RESERVATION_TIMEOUT_MINUTES);
        List<TicketReservationDTO> reservations = new ArrayList<>();

        for (ReserveTicketsRequest.Item item : items) {
            TicketReservationDTO reservation = inventoryReservationService.reserveTicket(
                    request.getBookingId(),
                    request.getUserId(),
                    item,
                    expiresAt
            );

            reservations.add(reservation);
        }

        return reservations;
    }

    private void validateUniqueTicketTypes(List<ReserveTicketsRequest.Item> items) {
        Set<String> ticketTypeIds = new HashSet<>();

        for (ReserveTicketsRequest.Item item : items) {
            if (!ticketTypeIds.add(item.getTicketTypeId())) {
                throw new IllegalStateException(
                        "Duplicate ticket type in reservation request: " + item.getTicketTypeId()
                );
            }
        }
    }
}
