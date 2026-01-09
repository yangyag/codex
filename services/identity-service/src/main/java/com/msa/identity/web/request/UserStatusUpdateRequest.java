package com.msa.identity.web.request;

import jakarta.validation.constraints.NotBlank;

public record UserStatusUpdateRequest(@NotBlank String status) {
}
