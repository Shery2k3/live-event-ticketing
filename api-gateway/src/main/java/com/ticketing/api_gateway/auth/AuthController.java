package com.ticketing.api_gateway.auth;

import com.ticketing.api_gateway.auth.dto.LoginRequest;
import com.ticketing.api_gateway.auth.dto.LoginResponse;
import com.ticketing.api_gateway.config.AppSecurityProperties;
import com.ticketing.api_gateway.config.AppSecurityProperties.AppUser;
import com.ticketing.api_gateway.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Minimal login endpoint, mounted under /api/auth to match the rest of the API.
 * Checks the request against the hardcoded users and, on a match, returns a
 * signed JWT. Passwords are compared in plain text here only because these are
 * demo credentials with no real user store.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AppSecurityProperties properties;
	private final JwtService jwtService;

	public AuthController(AppSecurityProperties properties, JwtService jwtService) {
		this.properties = properties;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
		List<AppUser> users = properties.users() == null ? List.of() : properties.users();
		if (users.isEmpty()) {
			log.warn("No users configured under 'security.users' - is config-server serving api-gateway.yaml?");
		}
		return Mono.justOrEmpty(users.stream()
						.filter(user -> user.username().equals(request.username())
								&& user.password().equals(request.password()))
						.findFirst())
				.map(user -> {
					log.info("Login succeeded for '{}' ({})", user.username(), user.role());
					String token = jwtService.issueToken(user.username(), user.role());
					return ResponseEntity.ok(new LoginResponse(
							token, "Bearer", user.role(), properties.jwt().expirationMinutes()));
				})
				.defaultIfEmpty(ResponseEntity.status(401).build())
				.doOnNext(response -> {
					if (response.getStatusCode().value() == 401) {
						log.warn("Login failed for username '{}'", request.username());
					}
				});
	}
}
