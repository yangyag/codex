package com.msa.board.application.command;

import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import java.util.List;

public record SearchBoardsCommand(
        String query,
        List<BoardVisibility> visibilities,
        List<BoardStatus> statuses,
        int page,
        int size) {
}
