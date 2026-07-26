package com.ticketing.booking.mapper;

import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.dto.BookingSeatResponse;
import com.ticketing.booking.entity.Booking;
import com.ticketing.booking.entity.BookingSeat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    BookingSeatResponse toSeatResponse(BookingSeat seat);
}