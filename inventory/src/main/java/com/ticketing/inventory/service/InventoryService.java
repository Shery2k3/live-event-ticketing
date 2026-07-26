package com.ticketing.inventory.service;

import com.ticketing.inventory.client.EventClient;
import com.ticketing.inventory.dto.AvailabilityResponse;
import com.ticketing.inventory.dto.CreateSeatsRequest;
import com.ticketing.inventory.dto.HoldSeatsRequest;
import com.ticketing.inventory.dto.SeatResponse;
import com.ticketing.inventory.entity.Seat;
import com.ticketing.inventory.entity.enums.SeatStatus;
import com.ticketing.inventory.exception.custom.ResourceNotFoundException;
import com.ticketing.inventory.exception.custom.SeatUnavailableException;
import com.ticketing.inventory.mapper.SeatMapper;
import com.ticketing.inventory.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class InventoryService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;
    private final EventClient eventClient;

    public InventoryService(SeatRepository seatRepository, SeatMapper seatMapper, EventClient eventClient) {
        this.seatRepository = seatRepository;
        this.seatMapper = seatMapper;
        this.eventClient = eventClient;
    }

    @Transactional
    public List<SeatResponse> createSeats(CreateSeatsRequest request) {
        eventClient.findEvent(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.eventId()));

        List<Seat> seats = request.seatNumbers().stream()
                .map(number -> Seat.builder()
                        .eventId(request.eventId())
                        .seatNumber(number)
                        .status(SeatStatus.AVAILABLE)
                        .build())
                .toList();

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        return seatMapper.toResponseList(savedSeats);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> listSeats(Long eventId) {
        return seatMapper.toResponseList(seatRepository.findByEventId(eventId));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(Long eventId) {
        return new AvailabilityResponse(
                eventId,
                seatRepository.countByEventIdAndStatus(eventId, SeatStatus.AVAILABLE),
                seatRepository.countByEventIdAndStatus(eventId, SeatStatus.HELD),
                seatRepository.countByEventIdAndStatus(eventId, SeatStatus.BOOKED));

    }

    // MArks the seat as HELD, uses @Version for optimistic locking
//    @Transactional
//    public SeatResponse holdSeat(Long seatId) {
//        Seat seat = seatRepository.findById(seatId)
//                .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));
//
//        if (seat.getStatus() != SeatStatus.AVAILABLE) {
//            throw new SeatUnavailableException(seatId);
//        }
//
//        seat.setStatus(SeatStatus.HELD);
//
//        try {
//            return seatMapper.toResponse(seatRepository.saveAndFlush(seat));
//        } catch (ObjectOptimisticLockingFailureException ex) {
//            // race lost to some other thread
//            throw new SeatUnavailableException(seatId);
//        }
//    }

    /**
     * Atomically holds EVERY requested seat, or none of them.
     * Called synchronously by Booking before any Kafka event is published.
     */
    @Transactional
    public List<SeatResponse> holdSeats(HoldSeatsRequest request) {
        List<Long> ids = request.seatIds().stream().distinct().sorted().toList();

        // Pessimistic locking here
        List<Seat> seats = seatRepository.lockSeatsForUpdate(ids);
        if (seats.size() != ids.size()) {
            throw new ResourceNotFoundException("Seat", ids);
        }

        // idempotency: this exact booking already holds these seats -> return as-is
        boolean alreadyHeldByThisBooking = seats.stream()
                .allMatch(s -> s.getStatus() == SeatStatus.HELD
                        && request.bookingReference().equals(s.getBookingReference()));
        if (alreadyHeldByThisBooking) {
            return seatMapper.toResponseList(seats);
        }

        seats.stream()
                .filter(s -> s.getStatus() != SeatStatus.AVAILABLE)
                .findFirst()
                .ifPresent(s -> { throw new SeatUnavailableException(s.getId()); });

        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.HELD);
            seat.setBookingReference(request.bookingReference());
        });

        log.info("Held {} seat(s) for booking {}", seats.size(), request.bookingReference());
        return seatMapper.toResponseList(seatRepository.saveAll(seats));
    }

    // Saga step: payment succeeded
    @Transactional
    public void confirmSeats(String bookingReference) {
        List<Seat> seats = seatRepository.findByBookingReference(bookingReference);
        if (seats.isEmpty()) {
            log.warn("confirmSeats: no seats found for booking {}", bookingReference);
            return;
        }
        seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.HELD)
                .forEach(s -> s.setStatus(SeatStatus.BOOKED));
        seatRepository.saveAll(seats);
        log.info("Confirmed {} seat(s) for booking {}", seats.size(), bookingReference);
    }

    // Saga COMPENSATION: payment failed
    @Transactional
    public void releaseSeats(String bookingReference) {
        List<Seat> seats = seatRepository.findByBookingReference(bookingReference);
        if (seats.isEmpty()) {
            log.warn("releaseSeats: no seats found for booking {}", bookingReference);
            return;
        }
        seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.HELD)
                .forEach(s -> {
                    s.setStatus(SeatStatus.AVAILABLE);
                    s.setBookingReference(null);
                });
        seatRepository.saveAll(seats);
        log.info("Released {} seat(s) for booking {}", seats.size(), bookingReference);
    }

    // Simple optimistic locking for a single seat hold.
    @Transactional
    public SeatResponse holdSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatUnavailableException(seatId);
        }
        seat.setStatus(SeatStatus.HELD);
        try {
            return seatMapper.toResponse(seatRepository.saveAndFlush(seat));
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new SeatUnavailableException(seatId);
        }
    }

}
