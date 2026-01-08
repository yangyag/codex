package com.msa.identity.web;

import com.msa.identity.application.AuthService;
import com.msa.identity.application.SignupService;
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

    private final SignupService signupService;
    private final AuthService authService;

    public AuthController(SignupService signupService, AuthService authService) {
        this.signupService = signupService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        User saved = signupService.signup(request);
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
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
