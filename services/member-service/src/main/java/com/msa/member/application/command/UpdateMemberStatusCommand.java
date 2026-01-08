package com.msa.member.application.command;

import java.util.UUID;

public record UpdateMemberStatusCommand(UUID memberId, String status) {
}
