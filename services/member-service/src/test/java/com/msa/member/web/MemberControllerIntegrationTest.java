package com.msa.member.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.msa.member.domain.Member;
import com.msa.member.domain.MemberRepository;
import com.msa.member.domain.MemberStatus;
import com.msa.member.web.request.MemberStatusUpdateRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import java.util.Map;
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
@EnabledIf(
        expression = "#{T(com.msa.member.web.MemberControllerIntegrationTest).dockerAvailable()}",
        loadContext = false
)
class MemberControllerIntegrationTest {

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
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Test
    void authenticated_user_can_search_members() {
        Member target = memberRepository.save(new Member("alpha@example.com", "Alpha", MemberStatus.ACTIVE));
        memberRepository.save(new Member("bravo@example.com", "Bravo", MemberStatus.ACTIVE));

        HttpHeaders headers = authHeader(adminToken());
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/members?q=alpha",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("email")).isEqualTo(target.getEmail());
    }

    @Test
    void unauthenticated_request_cannot_view_members() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/members",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sync_endpoint_creates_member_without_auth() {
        Map<String, String> body = Map.of(
                "email", "sync@example.com",
                "name", "sync-user"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/members/sync",
                body,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(memberRepository.findByEmail("sync@example.com")).isPresent();
    }

    @Test
    void admin_can_update_member_status() {
        Member member = memberRepository.save(new Member("status@example.com", "Status", MemberStatus.ACTIVE));

        MemberStatusUpdateRequest request = new MemberStatusUpdateRequest("BLOCKED");
        HttpHeaders headers = authHeader(adminToken());
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/members/" + member.getId() + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(memberRepository.findById(member.getId()))
                .get()
                .extracting(Member::getStatus)
                .isEqualTo(MemberStatus.BLOCKED);
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
}
