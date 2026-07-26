package com.ticketing.api_gateway.security;

import com.ticketing.api_gateway.config.AppSecurityProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Issues HS256-signed JWTs for the login endpoint. The role is carried in a
 * "roles" claim, which SecurityConfig maps to a ROLE_ authority on the way in.
 */
@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final AppSecurityProperties properties;

	public JwtService(JwtEncoder jwtEncoder, AppSecurityProperties properties) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
	}

	public String issueToken(String username, String role) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.jwt().issuer())
				.issuedAt(now)
				.expiresAt(now.plus(Duration.ofMinutes(properties.jwt().expirationMinutes())))
				.subject(username)
				.claim("roles", List.of(role))
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
