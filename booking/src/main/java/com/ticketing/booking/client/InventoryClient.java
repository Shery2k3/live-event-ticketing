package com.ticketing.booking.client;

import com.ticketing.booking.client.dto.HoldSeatsRequest;
import com.ticketing.booking.client.dto.SeatView;
import com.ticketing.booking.exception.custom.SeatsUnavailableException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient inventoryWebClient;

    /**
     * Synchronously asks Inventory to hold every seat, all-or-nothing.
     * This is the one step of the saga that is NOT event-driven: the caller is
     * an HTTP client waiting for an answer, and "seat already taken" must be an
     * immediate 409 rather than an email five seconds later.
     */
    public List<SeatView> holdSeats(String bookingReference, List<Long> seatIds) {
        try {
            return inventoryWebClient.post()
                    .uri("/api/inventory/seats/hold")
                    .bodyValue(new HoldSeatsRequest(bookingReference, seatIds))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SeatView>>() { })
                    .block();
        } catch (WebClientResponseException.Conflict ex) {
            throw new SeatsUnavailableException(seatIds);
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Seat");
        }
    }
}