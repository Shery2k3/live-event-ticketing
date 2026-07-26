package com.ticketing.notification.controller;

import com.ticketing.notification.dto.NotificationResponse;
import com.ticketing.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only history for the demo. Useful to show persisted notifications after
 * running a booking through Postman.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> all() {
        return notificationService.recent().stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/{bookingReference}")
    public List<NotificationResponse> forBooking(@PathVariable String bookingReference) {
        return notificationService.forBooking(bookingReference).stream().map(NotificationResponse::from).toList();
    }
}
