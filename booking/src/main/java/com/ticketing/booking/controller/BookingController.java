package com.ticketing.booking.controller;

import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.dto.CreateBookingRequest;
import com.ticketing.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse created = bookingService.createBooking(request);
        return ResponseEntity
                .accepted()                                  // 202, not 201
                .location(URI.create("/api/bookings/" + created.bookingReference()))
                .body(created);
    }

    @GetMapping("/{bookingReference}")
    public BookingResponse findByReference(@PathVariable String bookingReference) {
        return bookingService.findByReference(bookingReference);
    }

    @GetMapping
    public List<BookingResponse> findByUser(@RequestParam Long userId) {
        return bookingService.findByUser(userId);
    }
}