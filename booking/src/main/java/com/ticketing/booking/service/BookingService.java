package com.ticketing.booking.service;

import com.ticketing.booking.client.EventClient;
import com.ticketing.booking.client.InventoryClient;
import com.ticketing.booking.client.dto.EventView;
import com.ticketing.booking.client.dto.SeatView;
import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.dto.CreateBookingRequest;
import com.ticketing.booking.entity.Booking;
import com.ticketing.booking.entity.BookingSeat;
import com.ticketing.booking.entity.enums.BookingStatus;
import com.ticketing.booking.exception.custom.ResourceNotFoundException;
import com.ticketing.booking.mapper.BookingMapper;
import com.ticketing.booking.messaging.BookingEventPublisher;
import com.ticketing.booking.messaging.event.BookingConfirmedEvent;
import com.ticketing.booking.messaging.event.PaymentCompletedEvent;
import com.ticketing.booking.messaging.event.PaymentFailedEvent;
import com.ticketing.booking.messaging.event.SeatReservedEvent;
import com.ticketing.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final EventClient eventClient;
    private final InventoryClient inventoryClient;
    private final BookingEventPublisher publisher;

    /**
     * STEP 1 of the saga.
     *   a) validate the event exists and read its price      (sync, WebClient)
     *   b) hold the seats -- all or nothing                  (sync, WebClient)
     *   c) persist the booking as PENDING_PAYMENT            (local transaction)
     *   d) publish SeatReserved so Payment can pick it up    (async, Kafka)
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        EventView event = eventClient.findEvent(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.eventId()));

        String bookingReference = generateReference();

        // Throws 409 SeatsUnavailableException -> nothing persisted, nothing published.
        List<SeatView> heldSeats = inventoryClient.holdSeats(bookingReference, request.seatIds());

        BigDecimal unitPrice = event.ticketPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(heldSeats.size()));

        Booking booking = new Booking();
        booking.setBookingReference(bookingReference);
        booking.setUserId(request.userId());
        booking.setEventId(request.eventId());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalAmount(totalAmount);

        heldSeats.forEach(seat -> booking.addSeat(BookingSeat.builder()
                .seatId(seat.id())
                .seatNumber(seat.seatNumber())
                .price(unitPrice)
                .build()));

        Booking saved = bookingRepository.saveAndFlush(booking);
        log.info("Created booking {} ({} seats, total {})",
                bookingReference, heldSeats.size(), totalAmount);

        publisher.publishSeatReserved(new SeatReservedEvent(
                saved.getBookingReference(),
                saved.getId(),
                saved.getUserId(),
                saved.getEventId(),
                heldSeats.stream().map(SeatView::id).toList(),
                saved.getTotalAmount(),
                Instant.now()));

        return bookingMapper.toResponse(saved);
    }

    /** STEP 3a: payment succeeded -> confirm and fan out to Notification. */
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Booking booking = requireBooking(event.bookingReference());

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            log.info("Ignoring PaymentCompleted for {} -- already {}",
                    booking.getBookingReference(), booking.getStatus());
            return;                                     // idempotent replay guard
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.saveAndFlush(booking);
        log.info("Booking {} CONFIRMED", booking.getBookingReference());

        publisher.publishBookingConfirmed(new BookingConfirmedEvent(
                booking.getBookingReference(),
                booking.getId(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getSeats().stream().map(BookingSeat::getSeatId).toList(),
                booking.getTotalAmount(),
                Instant.now()));
    }

    /** STEP 3b: payment failed -> cancel. Inventory releases the seats in parallel. */
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Booking booking = requireBooking(event.bookingReference());

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            log.info("Ignoring PaymentFailed for {} -- already {}",
                    booking.getBookingReference(), booking.getStatus());
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setFailureReason(event.reason());
        bookingRepository.saveAndFlush(booking);
        log.info("Booking {} CANCELLED: {}", booking.getBookingReference(), event.reason());
    }

    @Transactional(readOnly = true)
    public BookingResponse findByReference(String bookingReference) {
        return bookingMapper.toResponse(requireBooking(bookingReference));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findByUser(Long userId) {
        return bookingMapper.toResponseList(bookingRepository.findByUserIdOrderByIdDesc(userId));
    }

    private Booking requireBooking(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingReference));
    }

    private String generateReference() {
        return "BKG-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }
}