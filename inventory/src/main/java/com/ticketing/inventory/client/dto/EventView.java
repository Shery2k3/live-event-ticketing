package com.ticketing.inventory.client.dto;

public record EventView(
        Long id,
        String name,
        Integer totalCapacity
) {
}
