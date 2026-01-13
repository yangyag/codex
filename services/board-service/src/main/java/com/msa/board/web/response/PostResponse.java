package com.msa.board.web.response;

import com.msa.board.domain.Post;
import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID boardId,
        String authorEmail,
        String title,
        String content,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getBoard().getId(),
                post.getAuthorEmail(),
                post.getTitle(),
                post.getContent(),
                post.getStatus().name(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
