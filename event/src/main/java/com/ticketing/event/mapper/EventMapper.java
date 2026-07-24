package com.ticketing.event.mapper;

import com.ticketing.event.dto.EventRequest;
import com.ticketing.event.dto.EventResponse;
import com.ticketing.event.entity.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {

    Event toEntity(EventRequest request);

    EventResponse toResponse(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(EventRequest request, @MappingTarget Event event);
}
