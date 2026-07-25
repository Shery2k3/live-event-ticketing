package com.ticketing.inventory.mapper;

import com.ticketing.inventory.dto.CreateSeatsRequest;
import com.ticketing.inventory.dto.SeatResponse;
import com.ticketing.inventory.entity.Seat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    SeatResponse toResponse(Seat seat);
    List<SeatResponse> toResponseList(List<Seat> seats);
    Seat toEntity(CreateSeatsRequest request);
}
