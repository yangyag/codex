package com.msa.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingIntegrationTest {

    private static final Key signingKey =
            Keys.hmacShaKeyFor("change-me-please-change-me-32bytes".getBytes(StandardCharsets.UTF_8));
    private static DisposableServer memberServer;
    private static final AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

    @LocalServerPort
    int port;

    private WebTestClient client;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        memberServer = HttpServer.create()
                .port(0)
                .route(routes -> routes
                        .get("/api/v1/members", (req, res) -> {
                            HttpHeaders headers = new HttpHeaders();
                            req.requestHeaders().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
                            capturedHeaders.set(headers);
                            return res.status(200)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .sendString(Mono.just("{\"status\":\"ok\"}"));
                        })
                        .get("/api/v1/boards", (req, res) -> res.status(200)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .sendString(Mono.just("{\"board\":\"ok\"}"))))
                .bindNow();
        registry.add("MEMBER_SERVICE_URL", () -> "http://localhost:" + memberServer.port());
        registry.add("IDENTITY_SERVICE_URL", () -> "http://localhost:" + memberServer.port());
        registry.add("BOARD_SERVICE_URL", () -> "http://localhost:" + memberServer.port());
        registry.add("JWT_SECRET", () -> "change-me-please-change-me-32bytes");
    }

    @AfterAll
    static void tearDown() {
        if (memberServer != null) {
            memberServer.disposeNow();
        }
    }

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        capturedHeaders.set(null);
    }

    @Test
    void rejectsMissingToken() {
        client.get()
                .uri("/api/v1/members")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void rejectsNonAdminRole() {
        String token = createToken("user@example.com", "USER");

        client.get()
                .uri("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(capturedHeaders.get()).isNull();
    }

    @Test
    void forwardsRequestWhenAdminTokenProvided() {
        String token = createToken("admin@example.com", "ADMIN");

        client.get()
                .uri("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"status\":\"ok\"}");

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers).isNotNull();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + token);
        assertThat(headers.getFirst("X-Auth-Email")).isEqualTo("admin@example.com");
        assertThat(headers.getFirst("X-Auth-Role")).isEqualTo("ADMIN");
    }

    @Test
    void forwardsBoardRequestForUserRole() {
        String token = createToken("reader@example.com", "USER");

        client.get()
                .uri("/api/v1/boards")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"board\":\"ok\"}");
    }

    private String createToken(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
