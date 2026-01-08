package com.msa.identity.domain;

import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createUser(String email, String passwordHash) {
        return new User(email, passwordHash, UserRole.USER, UserStatus.ACTIVE);
    }
}
