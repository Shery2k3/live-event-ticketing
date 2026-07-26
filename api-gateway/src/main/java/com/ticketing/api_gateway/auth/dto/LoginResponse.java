package com.ticketing.api_gateway.auth.dto;

public record LoginResponse(String token, String tokenType, String role, long expiresInMinutes) {
}
