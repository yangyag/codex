package com.msa.board.application.command;

import com.msa.board.domain.BoardVisibility;

public record CreateBoardCommand(String name, BoardVisibility visibility) {
}
