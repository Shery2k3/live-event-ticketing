package com.ticketing.notification.service;

import com.ticketing.notification.entity.Notification;
import com.ticketing.notification.entity.enums.NotificationType;
import com.ticketing.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /**
     * Persists a notification for a saga event. Kafka delivers at-least-once, so
     * the (eventType, bookingReference) pair is deduplicated here: a redelivered
     * event is skipped rather than notified twice.
     */
    @Transactional
    public void record(NotificationType type, String bookingReference, String message) {
        if (repository.existsByEventTypeAndBookingReference(type, bookingReference)) {
            log.info("Skipping duplicate {} notification for booking {}", type, bookingReference);
            return;
        }

        Notification notification = new Notification();
        notification.setEventType(type);
        notification.setBookingReference(bookingReference);
        notification.setMessage(message);
        repository.save(notification);

        log.info("NOTIFICATION | {}", message);
    }

    @Transactional(readOnly = true)
    public List<Notification> recent() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Notification> forBooking(String bookingReference) {
        return repository.findByBookingReferenceOrderByCreatedAtDesc(bookingReference);
    }
}
