package com.msa.board.web.request;

import com.msa.board.application.command.UpdateBoardCommand;
import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateBoardRequest(
        @Size(min = 1) String name,
        String visibility,
        String status
) {
    public UpdateBoardCommand toCommand(UUID boardId) {
        BoardVisibility vis = visibility != null ? BoardVisibility.valueOf(visibility.toUpperCase()) : null;
        BoardStatus st = status != null ? BoardStatus.valueOf(status.toUpperCase()) : null;
        return new UpdateBoardCommand(boardId, name, vis, st);
    }
}
