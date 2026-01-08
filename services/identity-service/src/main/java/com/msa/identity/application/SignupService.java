package com.msa.identity.application;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import com.msa.identity.web.exception.EmailAlreadyUsedException;
import com.msa.identity.web.request.SignupRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(request.email(), hashedPassword, UserRole.USER, UserStatus.ACTIVE);
        return userRepository.save(newUser);
    }
}
