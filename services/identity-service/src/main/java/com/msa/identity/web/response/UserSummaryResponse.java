package com.msa.identity.web.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String role,
        String status,
        OffsetDateTime createdAt
) {
}
