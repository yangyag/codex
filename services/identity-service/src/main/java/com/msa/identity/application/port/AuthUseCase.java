package com.msa.identity.application.port;

import com.msa.identity.application.command.LoginCommand;
import com.msa.identity.web.response.AuthResponse;

public interface AuthUseCase {
    AuthResponse login(LoginCommand command);
}
