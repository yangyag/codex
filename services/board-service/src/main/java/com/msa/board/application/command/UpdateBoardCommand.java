package com.msa.board.application.command;

import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import java.util.UUID;

public record UpdateBoardCommand(UUID boardId, String name, BoardVisibility visibility, BoardStatus status) {
}
