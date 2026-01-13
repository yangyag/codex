package com.msa.board.application.command;

import com.msa.board.domain.PostStatus;
import java.util.UUID;

public record CreatePostCommand(UUID boardId, String title, String content, PostStatus status, String authorEmail) {
}
