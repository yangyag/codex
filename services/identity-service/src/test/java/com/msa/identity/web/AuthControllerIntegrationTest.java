package com.msa.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.web.request.SignupRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@EnabledIf(
        expression = "#{T(com.msa.identity.web.AuthControllerIntegrationTest).dockerAvailable()}",
        loadContext = false
)
class AuthControllerIntegrationTest {

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
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterAll
    void stopContainer() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Test
    void signup_returns_201_on_success() {
        SignupRequest request = new SignupRequest("integration@example.com", "password123");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/auth/signup",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("email")).isEqualTo(request.email());

        User persisted = userRepository.findByEmail(request.email()).orElseThrow();
        assertThat(persisted.getEmail()).isEqualTo(request.email());
    }

    @Test
    void login_returns_token_on_success() {
        SignupRequest signup = new SignupRequest("login-success@example.com", "password123");
        restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/signup", signup, Map.class);

        Map<String, String> loginRequest = Map.of(
                "email", signup.email(),
                "password", signup.password()
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/login",
                loginRequest,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token")).isNotNull();
        assertThat(response.getBody().get("email")).isEqualTo(signup.email());
    }

    @Test
    void login_returns_401_when_password_incorrect() {
        SignupRequest signup = new SignupRequest("login-fail@example.com", "password123");
        restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/signup", signup, Map.class);

        Map<String, String> loginRequest = Map.of(
                "email", signup.email(),
                "password", "wrong-password"
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/login",
                loginRequest,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void signup_returns_409_on_duplicate_email() {
        SignupRequest signup = new SignupRequest("duplicate@example.com", "password123");
        restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/signup", signup, Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/signup",
                signup,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void signup_returns_400_on_invalid_input() {
        Map<String, String> badRequest = Map.of(
                "email", "",
                "password", ""
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/signup",
                badRequest,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
