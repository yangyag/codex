package com.msa.identity.application;

import com.msa.identity.domain.User;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.domain.UserRole;
import com.msa.identity.domain.UserStatus;
import com.msa.identity.web.exception.EmailAlreadyUsedException;
import com.msa.identity.web.request.SignupRequest;
import com.msa.identity.integration.MemberSyncClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberSyncClient memberSyncClient;

    public SignupService(UserRepository userRepository, PasswordEncoder passwordEncoder, MemberSyncClient memberSyncClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberSyncClient = memberSyncClient;
    }

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(request.email(), hashedPassword, UserRole.USER, UserStatus.ACTIVE);
        User saved = userRepository.save(newUser);
        String name = request.email().split("@")[0];
        memberSyncClient.syncMember(request.email(), name);
        return saved;
    }
}
