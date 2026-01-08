package com.msa.identity.config;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void seedUsers() {
        if (!userRepository.existsByEmail("admin")) {
            User admin = new User("admin", passwordEncoder.encode("yangyag1!"), UserRole.ADMIN, UserStatus.ACTIVE);
            userRepository.save(admin);
        }

        long currentCount = userRepository.count();
        long target = 101; // admin + 100 users
        if (currentCount >= target) {
            return;
        }

        List<User> batch = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String email = String.format("user%03d@example.com", i);
            if (userRepository.existsByEmail(email)) {
                continue;
            }
            User user = new User(email, passwordEncoder.encode("password123"), UserRole.USER, UserStatus.ACTIVE);
            batch.add(user);
        }
        if (!batch.isEmpty()) {
            userRepository.saveAll(batch);
        }
    }
}
