package com.msa.board.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    Page<Board> findByNameContainingIgnoreCaseAndVisibilityInAndStatusIn(
            String name, Iterable<BoardVisibility> visibilities, Iterable<BoardStatus> statuses, Pageable pageable);

    Page<Board> findByVisibilityInAndStatusIn(
            Iterable<BoardVisibility> visibilities, Iterable<BoardStatus> statuses, Pageable pageable);
}
