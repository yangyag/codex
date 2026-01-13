package com.msa.board.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.msa.board.domain.Board;
import com.msa.board.domain.BoardRepository;
import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@EnabledIf(expression = "#{T(com.msa.board.web.BoardControllerIntegrationTest).dockerAvailable()}", loadContext = false)
class BoardControllerIntegrationTest {

    private static final String TEST_SECRET = "change-me-please-change-me-32bytes";
    private static PostgreSQLContainer<?> postgres;

    public static boolean dockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("msa")
                .withUsername("msa")
                .withPassword("msa-password");
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> TEST_SECRET);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BoardRepository boardRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void clean() {
        boardRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Test
    void admin_can_create_board() {
        Map<String, String> request = Map.of(
                "name", "Announcements",
                "visibility", "PUBLIC"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/boards",
                new HttpEntity<>(request, authHeader(adminToken())),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(boardRepository.count()).isEqualTo(1);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Announcements");
    }

    @Test
    void non_admin_cannot_create_board() {
        Map<String, String> request = Map.of(
                "name", "Private",
                "visibility", "PRIVATE"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/boards",
                new HttpEntity<>(request, authHeader(userToken())),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(boardRepository.count()).isZero();
    }

    @Test
    void user_can_list_boards() {
        boardRepository.save(new Board("General", BoardVisibility.PUBLIC));
        boardRepository.save(new Board("Internal", BoardVisibility.PRIVATE));

        HttpHeaders headers = authHeader(userToken());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/boards?page=0&size=10&visibility=PUBLIC",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        var content = (java.util.List<Map<String, Object>>) body.get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("visibility")).isEqualTo("PUBLIC");
    }

    @Test
    void admin_can_update_board_status() {
        Board board = boardRepository.save(new Board("News", BoardVisibility.PUBLIC));

        Map<String, String> body = Map.of("status", "INACTIVE");
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/boards/" + board.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(body, authHeader(adminToken())),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Board updated = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BoardStatus.INACTIVE);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders authHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    private String adminToken() {
        return Jwts.builder()
                .setSubject("admin@example.com")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String userToken() {
        return Jwts.builder()
                .setSubject("user@example.com")
                .claim("role", "USER")
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
