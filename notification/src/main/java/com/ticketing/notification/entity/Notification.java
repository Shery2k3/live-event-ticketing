package com.ticketing.notification.entity;

import com.ticketing.notification.entity.base.BaseEntity;
import com.ticketing.notification.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notification_event",
                columnNames = {"event_type", "booking_reference"}))
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private NotificationType eventType;

    @Column(name = "booking_reference", nullable = false, length = 64)
    private String bookingReference;

    @Column(nullable = false, length = 500)
    private String message;
}
