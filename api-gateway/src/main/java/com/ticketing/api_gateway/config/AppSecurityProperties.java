package com.ticketing.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binds the security.* block from api-gateway.yaml (served by config-server).
 * Users are hardcoded here on purpose: this project has no user-service or
 * user table. The gateway simply plays the role of a small identity provider.
 */
@ConfigurationProperties(prefix = "security")
public record AppSecurityProperties(Jwt jwt, List<AppUser> users) {

	public record Jwt(String secret, long expirationMinutes, String issuer) {
	}

	public record AppUser(String username, String password, String role) {
	}
}
