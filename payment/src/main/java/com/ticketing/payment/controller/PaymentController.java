package com.ticketing.payment.controller;

import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.exception.custom.ResourceNotFoundException;
import com.ticketing.payment.mapper.PaymentMapper;
import com.ticketing.payment.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only. Payments are created asynchronously via Kafka, never by an HTTP POST.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @GetMapping
    public List<PaymentResponse> all() {
        return paymentMapper.toResponseList(paymentRepository.findAll());
    }

    @GetMapping("/{bookingReference}")
    public PaymentResponse getByBookingReference(@PathVariable String bookingReference) {
        return paymentMapper.toResponse(paymentRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", bookingReference)));
    }
}
