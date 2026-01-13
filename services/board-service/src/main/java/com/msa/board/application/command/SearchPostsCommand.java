package com.msa.board.application.command;

import com.msa.board.domain.PostStatus;
import java.util.List;
import java.util.UUID;

public record SearchPostsCommand(
        UUID boardId,
        String authorEmail,
        List<PostStatus> statuses,
        int page,
        int size) {
}
