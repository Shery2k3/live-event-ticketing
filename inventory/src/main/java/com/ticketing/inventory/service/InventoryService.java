package com.ticketing.inventory.service;

import com.ticketing.inventory.SeatRepository;
import com.ticketing.inventory.client.EventClient;
import com.ticketing.inventory.dto.AvailabilityResponse;
import com.ticketing.inventory.dto.CreateSeatsRequest;
import com.ticketing.inventory.dto.SeatResponse;
import com.ticketing.inventory.entity.Seat;
import com.ticketing.inventory.entity.enums.SeatStatus;
import com.ticketing.inventory.exception.custom.ResourceNotFoundException;
import com.ticketing.inventory.exception.custom.SeatUnavailableException;
import com.ticketing.inventory.mapper.SeatMapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            // race lost to some other thread
            throw new SeatUnavailableException(seatId);
        }
    }

}
