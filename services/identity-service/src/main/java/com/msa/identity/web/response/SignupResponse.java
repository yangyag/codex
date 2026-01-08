package com.msa.identity.web.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SignupResponse(
        UUID id,
        String email,
        String role,
        String status,
        OffsetDateTime createdAt
) {
}
