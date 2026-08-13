package com.tickethub.booking.service;

import com.tickethub.booking.client.CatalogClient;
import com.tickethub.booking.client.InventoryClient;
import com.tickethub.booking.dto.*;
import com.tickethub.booking.entity.*;
import com.tickethub.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public BookingResponse createBooking(String userId, CreateBookingRequest request) {
        // 1. Initialize booking
        Booking booking = Booking.builder()
                .userId(userId)
                .status(BookingStatus.WAITING_FOR_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> items = new ArrayList<>();
        Set<String> ticketTypeIds = new HashSet<>();

        // 2. Process request items and calculate total amount
        for (BookingItemRequest itemReq : request.getItems()) {

            // Check duplicate ticket type
            if (!ticketTypeIds.add(itemReq.getTicketTypeId())) {
                throw new IllegalArgumentException(
                        "Duplicate ticket type: " + itemReq.getTicketTypeId()
                );
            }

            // Call Catalog Service via Feign Client
            TicketTypeResponse ticketType = catalogClient.getTicketTypeById(itemReq.getTicketTypeId());
            if (ticketType == null) {
                throw new RuntimeException("Ticket type not found: " + itemReq.getTicketTypeId());
            }

            BigDecimal itemPrice = ticketType.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            BookingItem item = BookingItem.builder()
                    .ticketTypeId(itemReq.getTicketTypeId())
                    .quantity(itemReq.getQuantity())
                    .price(itemPrice)
                    .booking(booking)
                    .build();

            items.add(item);
        }

        booking.setTotalAmount(totalAmount);
        booking.setItems(items);

        // 3. Add initial status history
        BookingStatusHistory history = BookingStatusHistory.builder()
                .status(BookingStatus.WAITING_FOR_PAYMENT)
                .remarks("Booking created, waiting for payment")
                .booking(booking)
                .build();
        booking.getStatusHistory().add(history);

        // 4. Save booking
        Booking saved = bookingRepository.saveAndFlush(booking);

        // 5. Call inventory to reserve ticket
        ReserveTicketsRequest reserveTicketsRequest = ReserveTicketsRequest.builder()
                .bookingId(saved.getId())
                .userId(userId)
                .items(request.getItems())
                .build();

        List<ReserveTicketResponse> reserveTicketResponses = inventoryClient.reserveTicket(reserveTicketsRequest);

        if (reserveTicketResponses == null
                || reserveTicketResponses.size() != saved.getItems().size()) {
            throw new IllegalStateException("Invalid reservation response from inventory");
        }

        Map<String, String> reservationIdByTicketType = new HashMap<>();
        for (ReserveTicketResponse resItem : reserveTicketResponses) {
            if (resItem == null
                    || resItem.getTicketTypeId() == null
                    || resItem.getId() == null) {
                throw new IllegalStateException("Inventory returned an invalid reservation response");
            }

            reservationIdByTicketType.put(resItem.getTicketTypeId(), resItem.getId());
        }

        saved.getItems().forEach(item -> {
            String reservationId = reservationIdByTicketType.get(item.getTicketTypeId());
            if (reservationId == null) {
                throw new IllegalStateException(
                        "Missing reservation for ticket type: " + item.getTicketTypeId()
                );
            }

            item.setReservationId(reservationId);
        });

        return mapToBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String userId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to this booking");
        }

        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String userId, String bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToBookingResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.WAITING_FOR_PAYMENT) {
            throw new RuntimeException("Cannot cancel booking in status: " + booking.getStatus());
        }

        inventoryClient.releaseReservations(getReservationIds(booking));

        booking.setStatus(BookingStatus.CANCELLED);
        
        BookingStatusHistory history = BookingStatusHistory.builder()
                .status(BookingStatus.CANCELLED)
                .remarks("Booking cancelled by user")
                .booking(booking)
                .build();
        booking.getStatusHistory().add(history);

        // No need to call booking.save() (hibernate dirty checking)

        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToBookingResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.WAITING_FOR_PAYMENT
                && booking.getStatus() != BookingStatus.PAYMENT_PROCESSING) {
            throw new RuntimeException("Cannot confirm booking in status: " + booking.getStatus());
        }

        inventoryClient.confirmReservations(getReservationIds(booking));

        booking.setStatus(BookingStatus.CONFIRMED);

        BookingStatusHistory history = BookingStatusHistory.builder()
                .status(BookingStatus.CONFIRMED)
                .remarks("Payment completed, booking confirmed")
                .booking(booking)
                .build();
        booking.getStatusHistory().add(history);

        return mapToBookingResponse(booking);
    }

    private List<String> getReservationIds(Booking booking) {
        List<String> reservationIds = booking.getItems().stream()
                .map(BookingItem::getReservationId)
                .toList();

        if (reservationIds.isEmpty()
                || reservationIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalStateException("Booking contains invalid reservation IDs");
        }

        return reservationIds;
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingItemResponse> itemDTOs = booking.getItems().stream()
                .map(item -> BookingItemResponse.builder()
                        .id(item.getId())
                        .ticketTypeId(item.getTicketTypeId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .status(booking.getStatus().name())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
}
