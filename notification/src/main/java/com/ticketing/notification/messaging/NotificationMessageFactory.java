package com.ticketing.notification.messaging;

import com.ticketing.notification.messaging.event.BookingConfirmedEvent;
import com.ticketing.notification.messaging.event.PaymentCompletedEvent;
import com.ticketing.notification.messaging.event.PaymentFailedEvent;
import com.ticketing.notification.messaging.event.SeatReservedEvent;
import org.springframework.stereotype.Component;

/**
 * Turns saga events into short, human-readable lines. Kept deliberately plain so
 * the messages read naturally in the service log during a demo.
 */
@Component
public class NotificationMessageFactory {

    public String seatReserved(SeatReservedEvent event) {
        return "Booking %s: %d seat(s) reserved for event %d, total %s. Awaiting payment."
                .formatted(event.bookingReference(), event.seatIds().size(), event.eventId(), event.totalAmount());
    }

    public String paymentCompleted(PaymentCompletedEvent event) {
        return "Booking %s: payment of %s completed (payment #%d)."
                .formatted(event.bookingReference(), event.amount(), event.paymentId());
    }

    public String paymentFailed(PaymentFailedEvent event) {
        return "Booking %s: payment failed - %s. The booking was cancelled and the seats were released."
                .formatted(event.bookingReference(), event.reason());
    }

    public String bookingConfirmed(BookingConfirmedEvent event) {
        return "Booking %s confirmed: %d seat(s) for event %d, total %s. See you at the event."
                .formatted(event.bookingReference(), event.seatIds().size(), event.eventId(), event.totalAmount());
    }
}
