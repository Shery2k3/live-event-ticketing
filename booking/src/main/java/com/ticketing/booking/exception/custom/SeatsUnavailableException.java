package com.ticketing.booking.exception.custom;

import java.util.List;

public class SeatsUnavailableException extends RuntimeException {
    public SeatsUnavailableException(List<Long> seatIds) {
        super("One or more seats are no longer available: %s".formatted(seatIds));
    }
}
