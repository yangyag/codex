package com.msa.board.web.response;

import com.msa.board.domain.Board;
import java.time.Instant;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        String name,
        String visibility,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getVisibility().name(),
                board.getStatus().name(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
