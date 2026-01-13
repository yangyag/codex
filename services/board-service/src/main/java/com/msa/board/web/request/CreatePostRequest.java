package com.msa.board.web.request;

import com.msa.board.application.command.CreatePostCommand;
import com.msa.board.domain.PostStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        String status
) {
    public CreatePostCommand toCommand(UUID boardId, String authorEmail) {
        PostStatus resolvedStatus = status != null ? PostStatus.valueOf(status.toUpperCase()) : PostStatus.DRAFT;
        return new CreatePostCommand(boardId, title.trim(), content.trim(), resolvedStatus, authorEmail);
    }
}
