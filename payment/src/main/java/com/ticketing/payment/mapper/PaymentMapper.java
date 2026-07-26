package com.ticketing.payment.mapper;

import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);
    List<PaymentResponse> toResponseList(List<Payment> payments);
}
