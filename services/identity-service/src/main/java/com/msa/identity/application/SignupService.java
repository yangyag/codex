package com.msa.identity.application;

import com.msa.identity.application.command.SignupCommand;
import com.msa.identity.application.port.MemberSyncPort;
import com.msa.identity.application.port.SignupUseCase;
import com.msa.identity.domain.User;
import com.msa.identity.domain.UserFactory;
import com.msa.identity.domain.UserRepository;
import com.msa.identity.web.exception.EmailAlreadyUsedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberSyncPort memberSyncPort;
    private final UserFactory userFactory;

    public SignupService(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         MemberSyncPort memberSyncPort,
                         UserFactory userFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberSyncPort = memberSyncPort;
        this.userFactory = userFactory;
    }

    @Override
    @Transactional
    public User signup(SignupCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyUsedException(command.email());
        }

        String hashedPassword = passwordEncoder.encode(command.password());
        User newUser = userFactory.createUser(command.email(), hashedPassword);
        User saved = userRepository.save(newUser);
        String name = command.email().split("@")[0];
        memberSyncPort.syncMember(command.email(), name);
        return saved;
    }
}
