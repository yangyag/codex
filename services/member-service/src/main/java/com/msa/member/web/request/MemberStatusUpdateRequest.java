package com.msa.member.web.request;

import jakarta.validation.constraints.NotBlank;

public record MemberStatusUpdateRequest(
        @NotBlank String status
) {
}
