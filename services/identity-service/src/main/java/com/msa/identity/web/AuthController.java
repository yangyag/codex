package com.msa.identity.web;

import com.msa.identity.application.command.LoginCommand;
import com.msa.identity.application.command.SignupCommand;
import com.msa.identity.application.port.AuthUseCase;
import com.msa.identity.application.port.SignupUseCase;
import com.msa.identity.domain.User;
import com.msa.identity.web.request.LoginRequest;
import com.msa.identity.web.request.SignupRequest;
import com.msa.identity.web.response.AuthResponse;
import com.msa.identity.web.response.SignupResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final AuthUseCase authUseCase;

    public AuthController(SignupUseCase signupUseCase, AuthUseCase authUseCase) {
        this.signupUseCase = signupUseCase;
        this.authUseCase = authUseCase;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        User saved = signupUseCase.signup(new SignupCommand(request.email(), request.password()));
        SignupResponse response = new SignupResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        );
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(response);
    }
}
