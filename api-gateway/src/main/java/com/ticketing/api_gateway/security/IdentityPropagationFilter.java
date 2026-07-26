package com.ticketing.api_gateway.security;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Runs after Spring Security. It always strips any client-supplied identity
 * headers so they cannot be spoofed, and for authenticated requests it injects
 * the caller's id and roles as headers the downstream services can read.
 */
@Component
public class IdentityPropagationFilter implements WebFilter, Ordered {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String USER_ROLES_HEADER = "X-User-Roles";

	@Override
	public int getOrder() {
		// Spring Security's WebFilterChainProxy runs at -100, so 0 runs after it,
		// once the security context has been populated.
		return 0;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest sanitized = exchange.getRequest().mutate()
				.headers(headers -> {
					headers.remove(USER_ID_HEADER);
					headers.remove(USER_ROLES_HEADER);
				})
				.build();
		ServerWebExchange base = exchange.mutate().request(sanitized).build();

		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.filter(JwtAuthenticationToken.class::isInstance)
				.cast(JwtAuthenticationToken.class)
				.flatMap(auth -> {
					String roles = auth.getAuthorities().stream()
							.map(GrantedAuthority::getAuthority)
							.collect(Collectors.joining(","));
					ServerHttpRequest withIdentity = base.getRequest().mutate()
							.header(USER_ID_HEADER, auth.getToken().getSubject())
							.header(USER_ROLES_HEADER, roles)
							.build();
					return chain.filter(base.mutate().request(withIdentity).build());
				})
				.switchIfEmpty(chain.filter(base));
	}
}
