package com.tickethub.inventory.service;

import com.tickethub.inventory.dto.ReserveTicketsRequest;
import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.entity.ReservationStatus;
import com.tickethub.inventory.entity.TicketReservation;
import com.tickethub.inventory.mapper.InventoryMapper;
import com.tickethub.inventory.repository.TicketInventoryRepository;
import com.tickethub.inventory.repository.TicketReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final TicketInventoryRepository ticketInventoryRepository;
    private final TicketReservationRepository ticketReservationRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public TicketReservationDTO reserveTicket(
            String bookingId,
            String userId,
            ReserveTicketsRequest.Item item,
            LocalDateTime expiresAt
    ) {
        TicketReservation existingReservation = ticketReservationRepository
                .findByBookingIdAndTicketTypeId(bookingId, item.getTicketTypeId())
                .orElse(null);

        if (existingReservation != null) {
            if (isSameActiveReservation(existingReservation, userId, item)) {
                return inventoryMapper.toReservationResponse(existingReservation);
            }

            throw new IllegalStateException(
                    "Cannot reserve tickets for booking " + bookingId
                            + " and ticket type " + item.getTicketTypeId()
                            + " because an incompatible reservation already exists"
            );
        }

        reserveInventory(item);

        TicketReservation reservation = TicketReservation.builder()
                .bookingId(bookingId)
                .userId(userId)
                .ticketTypeId(item.getTicketTypeId())
                .quantity(item.getQuantity())
                .status(ReservationStatus.RESERVED)
                .expiresAt(expiresAt)
                .build();

        TicketReservation savedReservation = ticketReservationRepository.save(reservation);

        return inventoryMapper.toReservationResponse(savedReservation);
    }

    private boolean isSameActiveReservation(
            TicketReservation reservation,
            String userId,
            ReserveTicketsRequest.Item item
    ) {
        return reservation.getStatus() == ReservationStatus.RESERVED
                && reservation.getUserId().equals(userId)
                && reservation.getQuantity().equals(item.getQuantity())
                && reservation.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private void reserveInventory(ReserveTicketsRequest.Item item) {
        int updatedRows = ticketInventoryRepository.reserveTickets(
                item.getTicketTypeId(),
                item.getQuantity()
        );

        if (updatedRows > 0) {
            return;
        }

        if (ticketInventoryRepository.findByTicketTypeId(item.getTicketTypeId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory not found for ticket type: " + item.getTicketTypeId()
            );
        }

        throw new IllegalStateException(
                "Not enough tickets available for ticket type: " + item.getTicketTypeId()
        );
    }

    @Transactional
    public TicketReservationDTO confirmReservation(String reservationId) {
        TicketReservation reservation = ticketReservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId
                ));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return inventoryMapper.toReservationResponse(reservation);
        }

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
        TicketReservation reservation = ticketReservationRepository
                .findByIdForUpdate(reservationId)
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
