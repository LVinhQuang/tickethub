package com.tickethub.inventory.repository;

import com.tickethub.inventory.entity.ReservationStatus;
import com.tickethub.inventory.entity.TicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketReservationRepository extends JpaRepository<TicketReservation, String> {

    Optional<TicketReservation> findByBookingIdAndTicketTypeId(String bookingId, String ticketTypeId);

    List<TicketReservation> findByBookingId(String bookingId);

    List<TicketReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime expiresAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE TicketReservation reservation
        SET reservation.status = :expiredStatus
        WHERE reservation.id = :reservationId
          AND reservation.status = :reservedStatus
          AND reservation.expiresAt <= :now
        """)
    int markExpired(
            @Param("reservationId") String reservationId,
            @Param("reservedStatus") ReservationStatus reservedStatus,
            @Param("expiredStatus") ReservationStatus expiredStatus,
            @Param("now") LocalDateTime now
    );
}
