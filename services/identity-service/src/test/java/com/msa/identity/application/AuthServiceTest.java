package com.msa.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.msa.identity.application.command.LoginCommand;
import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import com.msa.identity.security.JwtProvider;
import com.msa.identity.web.exception.InvalidCredentialsException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void login_returns_token_when_credentials_valid() {
        LoginCommand command = new LoginCommand("user@example.com", "password123");
        User user = new User(command.email(), passwordEncoder.encode(command.password()), UserRole.ADMIN, UserStatus.ACTIVE);
        given(userRepository.findByEmail(command.email())).willReturn(Optional.of(user));
        given(jwtProvider.generateToken(user.getEmail(), user.getRole())).willReturn("issued-token");

        var response = authService.login(command);

        assertThat(response.token()).isEqualTo("issued-token");
        assertThat(response.email()).isEqualTo(command.email());
        verify(jwtProvider).generateToken(command.email(), user.getRole());
    }

    @Test
    void login_throws_when_user_not_found() {
        LoginCommand command = new LoginCommand("missing@example.com", "password123");
        given(userRepository.findByEmail(command.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_when_password_invalid() {
        LoginCommand command = new LoginCommand("user@example.com", "password123");
        User user = new User(command.email(), passwordEncoder.encode("otherPassword"), UserRole.USER, UserStatus.ACTIVE);
        given(userRepository.findByEmail(command.email())).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
