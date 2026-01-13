package com.msa.board.application.command;

import com.msa.board.domain.PostStatus;
import java.util.UUID;

public record UpdatePostCommand(
        UUID boardId,
        UUID postId,
        String title,
        String content,
        PostStatus status,
        String requesterEmail,
        boolean isAdmin) {
}
