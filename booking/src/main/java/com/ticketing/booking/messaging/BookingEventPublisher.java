package com.ticketing.booking.messaging;

import com.ticketing.booking.messaging.event.BookingConfirmedEvent;
import com.ticketing.booking.messaging.event.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    public static final String TOPIC_SEAT_RESERVED = "seat-reserved";
    public static final String TOPIC_BOOKING_CONFIRMED = "booking-confirmed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatReserved(SeatReservedEvent event) {
        kafkaTemplate.send(TOPIC_SEAT_RESERVED, event.bookingReference(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish SeatReserved for {}", event.bookingReference(), ex);
                    } else {
                        log.info("Published SeatReserved for {} to partition {}",
                                event.bookingReference(),
                                result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        kafkaTemplate.send(TOPIC_BOOKING_CONFIRMED, event.bookingReference(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BookingConfirmed for {}", event.bookingReference(), ex);
                    } else {
                        log.info("Published BookingConfirmed for {}", event.bookingReference());
                    }
                });
    }
}