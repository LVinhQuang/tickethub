package com.tickethub.inventory.service;

import com.tickethub.inventory.dto.CreateInventoryRequest;
import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.dto.ReserveTicketRequest;
import com.tickethub.inventory.dto.TicketInventoryResponse;
import com.tickethub.inventory.entity.ReservationStatus;
import com.tickethub.inventory.entity.TicketInventory;
import com.tickethub.inventory.entity.TicketReservation;
import com.tickethub.inventory.mapper.InventoryMapper;
import com.tickethub.inventory.repository.TicketInventoryRepository;
import com.tickethub.inventory.repository.TicketReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final long RESERVATION_TIMEOUT_MINUTES = 15;

    private final TicketInventoryRepository ticketInventoryRepository;
    private final TicketReservationRepository ticketReservationRepository;
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
    public TicketReservationDTO reserveTicket(ReserveTicketRequest request) {
        TicketReservation existingReservation = ticketReservationRepository
                .findByBookingIdAndTicketTypeId(request.getBookingId(), request.getTicketTypeId())
                .orElse(null);

        if (existingReservation != null) {
            if (existingReservation.getStatus() == ReservationStatus.RESERVED) {
                return inventoryMapper.toReservationResponse(existingReservation);
            }

            throw new IllegalStateException(
                    "Cannot reserve tickets for booking " + request.getBookingId()
                            + " because its reservation is " + existingReservation.getStatus()
            );
        }

        int updatedRows = ticketInventoryRepository.reserveTickets(
                request.getTicketTypeId(),
                request.getQuantity()
        );

        if (updatedRows == 0) {
            if (ticketInventoryRepository.findByTicketTypeId(request.getTicketTypeId()).isEmpty()) {
                throw new IllegalArgumentException(
                        "Inventory not found for ticket type: " + request.getTicketTypeId()
                );
            }

            throw new IllegalStateException(
                    "Not enough tickets available for ticket type: " + request.getTicketTypeId()
            );
        }

        TicketReservation reservation = TicketReservation.builder()
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .ticketTypeId(request.getTicketTypeId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.RESERVED)
                .expiresAt(LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES))
                .build();

        TicketReservation savedReservation = ticketReservationRepository.save(reservation);

        return inventoryMapper.toReservationResponse(savedReservation);
    }

    @Transactional
    public TicketReservationDTO confirmReservation(String reservationId) {
        TicketReservation reservation = ticketReservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED)
            return inventoryMapper.toReservationResponse(reservation);

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Cannot confirm reservation in status: " + reservation.getStatus()
            );
        }

        if (!reservation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Cannot confirm an expired reservation: " + reservationId
            );
        }

        int updatedRows = ticketInventoryRepository.confirmReservedTickets(
                reservation.getTicketTypeId(),
                reservation.getQuantity()
        );

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Unable to confirm reserved tickets for reservation: " + reservationId
            );
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        TicketReservation savedReservation = ticketReservationRepository.save(reservation);

        return inventoryMapper.toReservationResponse(savedReservation);
    }

    @Transactional
    public TicketReservationDTO releaseReservation(String reservationId) {
        TicketReservation reservation = ticketReservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId
                ));

        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            return inventoryMapper.toReservationResponse(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Cannot release reservation in status: " + reservation.getStatus()
            );
        }

        int updatedRows = ticketInventoryRepository.releaseReservedTickets(
                reservation.getTicketTypeId(),
                reservation.getQuantity()
        );

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Unable to release reserved tickets for reservation: " + reservationId
            );
        }

        reservation.setStatus(ReservationStatus.RELEASED);
        TicketReservation savedReservation = ticketReservationRepository.save(reservation);

        return inventoryMapper.toReservationResponse(savedReservation);
    }

    @Scheduled(fixedDelayString = "${inventory.reservation-expiration-check-delay-ms:60000}")
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<TicketReservation> reservations = ticketReservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, now);

        for (TicketReservation reservation : reservations) {
            int markedRows = ticketReservationRepository.markExpired(
                    reservation.getId(),
                    ReservationStatus.RESERVED,
                    ReservationStatus.EXPIRED,
                    now
            );

            // Reservation is confirmed/released by another flow.
            if (markedRows == 0) {
                continue;
            }

            int releasedRows = ticketInventoryRepository.releaseReservedTickets(
                    reservation.getTicketTypeId(),
                    reservation.getQuantity()
            );

            if (releasedRows == 0) {
                throw new IllegalStateException(
                        "Unable to release expired reservation: " + reservation.getId()
                );
            }
        }
    }
}
