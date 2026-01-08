package com.msa.member.web.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberSummaryResponse(
        UUID id,
        String email,
        String name,
        String status,
        OffsetDateTime createdAt
) {
}
