package com.msa.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {

    private final Key signingKey =
            Keys.hmacShaKeyFor("change-me-please-change-me-32bytes".getBytes(StandardCharsets.UTF_8));
    private JwtAuthenticationFilter filter;
    private GatewaySecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new GatewaySecurityProperties();
        filter = new JwtAuthenticationFilter(new JwtTokenValidator(signingKey), securityProperties);
    }

    @Test
    void shouldAllowOpenPathWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/auth/login").build());
        AtomicBoolean called = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            called.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(called).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectWhenTokenIsMissingOnProtectedPath() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/members").build());
        AtomicBoolean called = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            called.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(called).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldPassThroughWithValidToken() {
        String token = createToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build());
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<String> emailHeader = new AtomicReference<>();
        AtomicReference<String> roleHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            called.set(true);
            emailHeader.set(ex.getRequest().getHeaders().getFirst("X-Auth-Email"));
            roleHeader.set(ex.getRequest().getHeaders().getFirst("X-Auth-Role"));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(called).isTrue();
        assertThat(emailHeader.get()).isEqualTo("user@example.com");
        assertThat(roleHeader.get()).isEqualTo("ADMIN");
    }

    private String createToken() {
        return Jwts.builder()
                .setSubject("user@example.com")
                .claim("role", "ADMIN")
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
