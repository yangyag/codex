package com.msa.board.application;

import com.msa.board.application.command.CreateBoardCommand;
import com.msa.board.application.command.SearchBoardsCommand;
import com.msa.board.application.command.UpdateBoardCommand;
import com.msa.board.application.port.BoardUseCase;
import com.msa.board.domain.Board;
import com.msa.board.domain.BoardRepository;
import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import com.msa.board.domain.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoardService implements BoardUseCase {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Board createBoard(CreateBoardCommand command) {
        Board board = new Board(command.name(), command.visibility());
        return boardRepository.save(board);
    }

    @Override
    public Board updateBoard(UpdateBoardCommand command) {
        Board board = boardRepository.findById(command.boardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        board.update(command.name(), command.visibility(), command.status());
        return board;
    }

    @Override
    @Transactional(readOnly = true)
    public Board getBoard(UUID id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Board> searchBoards(SearchBoardsCommand command) {
        Pageable pageable = PageRequest.of(command.page(), command.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        List<BoardVisibility> visibilities = normalizeVisibilities(command.visibilities());
        List<BoardStatus> statuses = normalizeStatuses(command.statuses());
        String keyword = command.query() != null ? command.query().trim() : "";
        if (!keyword.isEmpty()) {
            return boardRepository.findByNameContainingIgnoreCaseAndVisibilityInAndStatusIn(
                    keyword, visibilities, statuses, pageable);
        }
        return boardRepository.findByVisibilityInAndStatusIn(visibilities, statuses, pageable);
    }

    private List<BoardVisibility> normalizeVisibilities(List<BoardVisibility> input) {
        if (input == null || input.isEmpty()) {
            return Arrays.asList(BoardVisibility.values());
        }
        return input;
    }

    private List<BoardStatus> normalizeStatuses(List<BoardStatus> input) {
        if (input == null || input.isEmpty()) {
            return Arrays.asList(BoardStatus.values());
        }
        return input;
    }
}
