package com.ticketing.booking.client.dto;

import java.util.List;

public record HoldSeatsRequest(
        String bookingReference,
        List<Long> seatIds
) {
}
