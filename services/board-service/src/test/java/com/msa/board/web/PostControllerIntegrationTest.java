package com.msa.board.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.msa.board.domain.Board;
import com.msa.board.domain.BoardRepository;
import com.msa.board.domain.BoardVisibility;
import com.msa.board.domain.Post;
import com.msa.board.domain.PostRepository;
import com.msa.board.domain.PostStatus;
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
@EnabledIf(expression = "#{T(com.msa.board.web.PostControllerIntegrationTest).dockerAvailable()}", loadContext = false)
class PostControllerIntegrationTest {

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

    @Autowired
    private PostRepository postRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void clean() {
        postRepository.deleteAll();
        boardRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Test
    void user_can_create_post_on_active_board() {
        Board board = boardRepository.save(new Board("QnA", BoardVisibility.PUBLIC));
        Map<String, String> req = Map.of(
                "title", "First",
                "content", "Hello",
                "status", "PUBLISHED"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/boards/" + board.getId() + "/posts",
                new HttpEntity<>(req, authHeader(userToken("author@example.com"))),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postRepository.count()).isEqualTo(1);
        assertThat(postRepository.findAll().get(0).getAuthorEmail()).isEqualTo("author@example.com");
    }

    @Test
    void non_author_cannot_update_post() {
        Board board = boardRepository.save(new Board("Board", BoardVisibility.PUBLIC));
        Post post = postRepository.save(new Post(board, "author@example.com", "t", "c", PostStatus.DRAFT));
        Map<String, String> req = Map.of(
                "title", "Updated"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/boards/" + board.getId() + "/posts/" + post.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(req, authHeader(userToken("other@example.com"))),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(postRepository.findById(post.getId())).get().extracting(Post::getTitle).isEqualTo("t");
    }

    @Test
    void admin_can_update_any_post() {
        Board board = boardRepository.save(new Board("Board", BoardVisibility.PUBLIC));
        Post post = postRepository.save(new Post(board, "author@example.com", "t", "c", PostStatus.DRAFT));
        Map<String, String> req = Map.of(
                "status", "PUBLISHED",
                "content", "new content"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/boards/" + board.getId() + "/posts/" + post.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(req, authHeader(adminToken())),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postRepository.findById(post.getId())).get().extracting(Post::getStatus).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void list_posts_can_filter_by_status() {
        Board board = boardRepository.save(new Board("Board", BoardVisibility.PUBLIC));
        postRepository.save(new Post(board, "author@example.com", "p1", "c1", PostStatus.PUBLISHED));
        postRepository.save(new Post(board, "author@example.com", "p2", "c2", PostStatus.DRAFT));

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/boards/" + board.getId() + "/posts?status=PUBLISHED",
                HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken("reader@example.com"))),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        var content = (java.util.List<Map<String, Object>>) body.get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("status")).isEqualTo("PUBLISHED");
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

    private String userToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", "USER")
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
