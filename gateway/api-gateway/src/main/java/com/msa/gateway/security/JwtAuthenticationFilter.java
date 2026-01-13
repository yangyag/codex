package com.msa.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator tokenValidator;
    private final GatewaySecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtTokenValidator tokenValidator, GatewaySecurityProperties securityProperties) {
        this.tokenValidator = tokenValidator;
        this.securityProperties = securityProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isOpenPath(path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        JwtUserClaims claims;
        try {
            claims = tokenValidator.validate(authorization.substring(7));
        } catch (JwtValidationException ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        if (requiresAdmin(path) && !"ADMIN".equalsIgnoreCase(claims.role())) {
            return forbidden(exchange, "ADMIN role required");
        }

        ServerHttpRequest authenticatedRequest = request.mutate()
                .header("X-Auth-Email", claims.email())
                .header("X-Auth-Role", claims.role())
                .build();

        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    private boolean isOpenPath(String path) {
        List<String> openPaths = securityProperties.getOpenPaths();
        return openPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean requiresAdmin(String path) {
        List<String> adminPaths = securityProperties.getAdminPaths();
        return adminPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return response.setComplete();
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return response.setComplete();
        }
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
