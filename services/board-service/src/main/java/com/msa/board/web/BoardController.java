package com.msa.board.web;

import com.msa.board.application.command.SearchBoardsCommand;
import com.msa.board.application.port.BoardUseCase;
import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.BoardVisibility;
import com.msa.board.web.request.CreateBoardRequest;
import com.msa.board.web.request.UpdateBoardRequest;
import com.msa.board.web.response.BoardResponse;
import com.msa.board.web.response.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardUseCase boardUseCase;

    public BoardController(BoardUseCase boardUseCase) {
        this.boardUseCase = boardUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BoardResponse createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return BoardResponse.from(boardUseCase.createBoard(request.toCommand()));
    }

    @GetMapping
    public PageResponse<BoardResponse> listBoards(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "visibility", required = false) List<String> visibilities,
            @RequestParam(name = "status", required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BoardVisibility> visibilityEnums = toVisibilities(visibilities);
        List<BoardStatus> statusEnums = toBoardStatuses(statuses);
        Page<BoardResponse> result = boardUseCase.searchBoards(
                        new SearchBoardsCommand(query, visibilityEnums, statusEnums, page, size))
                .map(BoardResponse::from);
        return PageResponse.from(result);
    }

    @GetMapping("/{id}")
    public BoardResponse getBoard(@PathVariable UUID id) {
        return BoardResponse.from(boardUseCase.getBoard(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BoardResponse updateBoard(@PathVariable UUID id, @Valid @RequestBody UpdateBoardRequest request) {
        return BoardResponse.from(boardUseCase.updateBoard(request.toCommand(id)));
    }

    private List<BoardVisibility> toVisibilities(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(v -> BoardVisibility.valueOf(v.toUpperCase())).toList();
    }

    private List<BoardStatus> toBoardStatuses(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(v -> BoardStatus.valueOf(v.toUpperCase())).toList();
    }
}
