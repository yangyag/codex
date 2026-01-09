package com.msa.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository);
    }

    @Test
    void updateStatus_updates_user_status() {
        User user = new User("user@example.com", "pw", UserRole.USER, UserStatus.ACTIVE);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

        var updated = adminUserService.updateStatus("user@example.com", "BLOCKED");

        assertThat(updated.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void updateStatus_throws_when_user_missing() {
        given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateStatus("missing@example.com", "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
