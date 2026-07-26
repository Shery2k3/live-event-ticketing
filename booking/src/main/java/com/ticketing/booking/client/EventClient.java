package com.ticketing.booking.client;

import com.ticketing.booking.client.dto.EventView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventClient {

    private final WebClient eventWebClient;

    public Optional<EventView> findEvent(Long eventId) {
        try {
            EventView event = eventWebClient.get()
                    .uri("/api/events/{id}", eventId)
                    .retrieve()
                    .bodyToMono(EventView.class)
                    .block();
            return Optional.ofNullable(event);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }
}