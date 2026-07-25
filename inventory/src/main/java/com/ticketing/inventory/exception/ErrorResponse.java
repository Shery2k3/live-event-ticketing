package com.ticketing.inventory.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(status, error, message, path, fieldErrors, Instant.now());
    }
}
