package com.ticketing.inventory.exception.custom;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(Long seatId) {
        super("Seat %d is not available".formatted(seatId));
    }
}
