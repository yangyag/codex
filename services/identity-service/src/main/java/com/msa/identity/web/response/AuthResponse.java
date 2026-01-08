package com.msa.identity.web.response;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}
