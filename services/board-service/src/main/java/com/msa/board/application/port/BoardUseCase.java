package com.msa.board.application.port;

import com.msa.board.application.command.CreateBoardCommand;
import com.msa.board.application.command.SearchBoardsCommand;
import com.msa.board.application.command.UpdateBoardCommand;
import com.msa.board.domain.Board;
import org.springframework.data.domain.Page;

public interface BoardUseCase {
    Board createBoard(CreateBoardCommand command);

    Board updateBoard(UpdateBoardCommand command);

    Board getBoard(java.util.UUID id);

    Page<Board> searchBoards(SearchBoardsCommand command);
}
