package com.ticketing.event.service;

import com.ticketing.event.dto.EventRequest;
import com.ticketing.event.dto.EventResponse;
import com.ticketing.event.entity.Event;
import com.ticketing.event.exception.custom.ResourceNotFoundException;
import com.ticketing.event.mapper.EventMapper;
import com.ticketing.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "events", key = "#id")
    @Transactional(readOnly = true)
    public EventResponse findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
         return eventMapper.toResponse(event);
    }

    @CachePut(value = "events", key = "#result.id()")
    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = eventMapper.toEntity(request);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @CachePut(value = "events", key = "#id")
    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        eventMapper.updateEntityFromRequest(request, event);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @CacheEvict(value = "events", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

}
