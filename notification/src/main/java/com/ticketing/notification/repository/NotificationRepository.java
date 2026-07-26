package com.ticketing.notification.repository;

import com.ticketing.notification.entity.Notification;
import com.ticketing.notification.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByEventTypeAndBookingReference(NotificationType eventType, String bookingReference);

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByBookingReferenceOrderByCreatedAtDesc(String bookingReference);
}
