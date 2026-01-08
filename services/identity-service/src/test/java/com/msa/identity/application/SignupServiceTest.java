package com.msa.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import com.msa.identity.integration.MemberSyncClient;
import com.msa.identity.web.exception.EmailAlreadyUsedException;
import com.msa.identity.web.request.SignupRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberSyncClient memberSyncClient;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupService(userRepository, passwordEncoder, memberSyncClient);
    }

    @Test
    void create_user_when_email_not_exists() {
        SignupRequest request = new SignupRequest("user@example.com", "password123");
        given(userRepository.existsByEmail(eq(request.email()))).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            try {
                var field = User.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(u, UUID.randomUUID());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return u;
        });

        User saved = signupService.signup(request);

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).save(any(User.class));
        verify(memberSyncClient).syncMember(request.email(), "user");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(request.email());
        assertThat(passwordEncoder.matches(request.password(), saved.getPasswordHash())).isTrue();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void throw_exception_when_email_exists() {
        SignupRequest request = new SignupRequest("user@example.com", "password123");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("이미 사용 중인 이메일");

        verify(memberSyncClient, never()).syncMember(any(), any());
    }
}
