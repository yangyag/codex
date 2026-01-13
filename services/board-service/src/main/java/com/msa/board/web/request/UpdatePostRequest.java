package com.msa.board.web.request;

import com.msa.board.application.command.UpdatePostCommand;
import com.msa.board.domain.PostStatus;
import java.util.UUID;

public record UpdatePostRequest(
        String title,
        String content,
        String status
) {
    public UpdatePostCommand toCommand(UUID boardId, UUID postId, String requesterEmail, boolean isAdmin) {
        PostStatus resolvedStatus = status != null ? PostStatus.valueOf(status.toUpperCase()) : null;
        return new UpdatePostCommand(boardId, postId, title, content, resolvedStatus, requesterEmail, isAdmin);
    }
}
