package com.msa.board.web.request;

import com.msa.board.application.command.CreateBoardCommand;
import com.msa.board.domain.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBoardRequest(
        @NotBlank String name,
        @NotNull String visibility
) {
    public CreateBoardCommand toCommand() {
        return new CreateBoardCommand(name.trim(), BoardVisibility.valueOf(visibility.toUpperCase()));
    }
}
