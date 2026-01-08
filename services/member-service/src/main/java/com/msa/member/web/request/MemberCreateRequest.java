package com.msa.member.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String name
) {
}
