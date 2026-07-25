package com.ticketing.inventory.client;

import com.ticketing.inventory.client.dto.EventView;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class EventClient {

    private final WebClient eventWebClient;

    public EventClient(WebClient eventWebClient) {
        this.eventWebClient = eventWebClient;
    }

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
