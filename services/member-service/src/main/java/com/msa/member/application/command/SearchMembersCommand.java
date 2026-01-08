package com.msa.member.application.command;

import org.springframework.data.domain.Pageable;

public record SearchMembersCommand(String query, Pageable pageable) {
}
