package com.msa.board.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByBoardId(UUID boardId, Pageable pageable);

    Page<Post> findByBoardIdAndStatusIn(UUID boardId, Iterable<PostStatus> statuses, Pageable pageable);

    Page<Post> findByBoardIdAndAuthorEmail(UUID boardId, String authorEmail, Pageable pageable);

    Page<Post> findByBoardIdAndAuthorEmailAndStatusIn(
            UUID boardId, String authorEmail, Iterable<PostStatus> statuses, Pageable pageable);
}
