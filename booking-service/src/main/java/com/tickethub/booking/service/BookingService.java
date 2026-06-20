package com.tickethub.booking.service;

import com.tickethub.booking.client.CatalogClient;
import com.tickethub.booking.dto.*;
import com.tickethub.booking.entity.*;
import com.tickethub.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public BookingResponseDTO createBooking(String userId, CreateBookingRequest request) {
        // 1. Initialize booking
        Booking booking = Booking.builder()
                .userId(userId)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> items = new ArrayList<>();

        // 2. Process request items and calculate total amount
        for (BookingItemRequest itemReq : request.getItems()) {
            // Call Catalog Service via Feign Client
            TicketTypeResponseDTO ticketType = catalogClient.getTicketTypeById(itemReq.getTicketTypeId());
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
                .status(BookingStatus.PENDING)
                .remarks("Booking created, pending payment")
                .booking(booking)
                .build();
        booking.getStatusHistory().add(history);

        // 4. Save to DB
        Booking saved = bookingRepository.save(booking);

        return mapToBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(String userId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to this booking");
        }

        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(String userId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to this booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel booking in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        
        BookingStatusHistory history = BookingStatusHistory.builder()
                .status(BookingStatus.CANCELLED)
                .remarks("Booking cancelled by user")
                .booking(booking)
                .build();
        booking.getStatusHistory().add(history);

        Booking saved = bookingRepository.save(booking);
        return mapToBookingResponse(saved);
    }

    private BookingResponseDTO mapToBookingResponse(Booking booking) {
        List<BookingItemResponseDTO> itemDTOs = booking.getItems().stream()
                .map(item -> BookingItemResponseDTO.builder()
                        .id(item.getId())
                        .ticketTypeId(item.getTicketTypeId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponseDTO.builder()
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
