package com.ticketing.inventory.controller;

import com.ticketing.inventory.dto.AvailabilityResponse;
import com.ticketing.inventory.dto.CreateSeatsRequest;
import com.ticketing.inventory.dto.SeatResponse;
import com.ticketing.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> createSeats(@Valid @RequestBody CreateSeatsRequest request) {
        return inventoryService.createSeats(request);
    }

    @GetMapping("/events/{eventId}/seats")
    public List<SeatResponse> listSeats(@PathVariable Long eventId) {
        return inventoryService.listSeats(eventId);
    }

    @GetMapping("/events/{eventId}/availability")
    public AvailabilityResponse availability(@PathVariable Long eventId) {
        return inventoryService.availability(eventId);
    }

    @PostMapping("/seats/{seatId}/hold")
    public SeatResponse hold(@PathVariable Long seatId) {
        return inventoryService.holdSeat(seatId);
    }
}