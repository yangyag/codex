package com.msa.identity.application.port;

import com.msa.identity.application.command.SignupCommand;
import com.msa.identity.domain.User;

public interface SignupUseCase {
    User signup(SignupCommand command);
}
