package com.ticketing.api_gateway.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.ticketing.api_gateway.config.AppSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * All authentication and authorization for the whole system lives here, at the
 * single edge. Downstream services trust the network behind the gateway and are
 * never exposed directly, so they carry no security code of their own.
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

	private final AppSecurityProperties properties;

	public SecurityConfig(AppSecurityProperties properties) {
		this.properties = properties;
	}

	private SecretKey secretKey() {
		byte[] bytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	public JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
	}

	@Bean
	public ReactiveJwtDecoder jwtDecoder() {
		return NimbusReactiveJwtDecoder.withSecretKey(secretKey())
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.authorizeExchange(exchange -> exchange
						// public: login + health
						.pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
						.pathMatchers("/actuator/health", "/actuator/info").permitAll()
						// public browsing: anyone can view events and seat availability
						.pathMatchers(HttpMethod.GET, "/api/events/**").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()
						// admin only: managing events and seat inventory
						.pathMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
						.pathMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
						.pathMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
						.pathMatchers("/api/inventory/**").hasRole("ADMIN")
						// any signed-in user can book and view their payments
						.pathMatchers("/api/bookings/**").authenticated()
						.pathMatchers("/api/payments/**").authenticated()
						.anyExchange().authenticated())
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return http.build();
	}

	private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("roles");
		authorities.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);

		return new ReactiveJwtAuthenticationConverterAdapter(converter);
	}
}
