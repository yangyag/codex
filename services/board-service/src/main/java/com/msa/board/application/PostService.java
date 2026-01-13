package com.msa.board.application;

import com.msa.board.application.command.CreatePostCommand;
import com.msa.board.application.command.SearchPostsCommand;
import com.msa.board.application.command.UpdatePostCommand;
import com.msa.board.application.port.PostUseCase;
import com.msa.board.domain.Board;
import com.msa.board.domain.BoardRepository;
import com.msa.board.domain.BoardStatus;
import com.msa.board.domain.Post;
import com.msa.board.domain.PostRepository;
import com.msa.board.domain.PostStatus;
import com.msa.board.domain.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostService implements PostUseCase {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    public PostService(BoardRepository boardRepository, PostRepository postRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Post createPost(CreatePostCommand command) {
        Board board = loadActiveBoard(command.boardId());
        Post post = new Post(board, command.authorEmail(), command.title(), command.content(), command.status());
        return postRepository.save(post);
    }

    @Override
    public Post updatePost(UpdatePostCommand command) {
        Post post = findPostInBoard(command.boardId(), command.postId());
        assertUpdatable(command.requesterEmail(), command.isAdmin(), post);
        post.update(command.title(), command.content(), command.status());
        return post;
    }

    @Override
    public void archivePost(UUID boardId, UUID postId, String requesterEmail, boolean isAdmin) {
        Post post = findPostInBoard(boardId, postId);
        assertUpdatable(requesterEmail, isAdmin, post);
        post.update(null, null, PostStatus.ARCHIVED);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPost(UUID boardId, UUID postId) {
        Post post = findPostInBoard(boardId, postId);
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> searchPosts(SearchPostsCommand command) {
        ensureBoardExists(command.boardId());
        Pageable pageable = PageRequest.of(command.page(), command.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostStatus> statuses = normalizeStatuses(command.statuses());
        boolean hasAuthor = command.authorEmail() != null && !command.authorEmail().isBlank();
        if (hasAuthor && statuses.size() > 0 && statuses.size() != PostStatus.values().length) {
            return postRepository.findByBoardIdAndAuthorEmailAndStatusIn(
                    command.boardId(), command.authorEmail(), statuses, pageable);
        }
        if (hasAuthor) {
            return postRepository.findByBoardIdAndAuthorEmail(command.boardId(), command.authorEmail(), pageable);
        }
        if (statuses.size() != PostStatus.values().length) {
            return postRepository.findByBoardIdAndStatusIn(command.boardId(), statuses, pageable);
        }
        return postRepository.findByBoardId(command.boardId(), pageable);
    }

    private void ensureBoardExists(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
    }

    private Board loadActiveBoard(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        if (board.getStatus() != BoardStatus.ACTIVE) {
            throw new IllegalStateException("Board is not active");
        }
        return board;
    }

    private Post findPostInBoard(UUID boardId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getBoard().getId().equals(boardId)) {
            throw new ResourceNotFoundException("Post not found");
        }
        return post;
    }

    private void assertUpdatable(String requesterEmail, boolean isAdmin, Post post) {
        if (isAdmin) {
            return;
        }
        if (!post.getAuthorEmail().equalsIgnoreCase(requesterEmail)) {
            throw new AccessDeniedException("Only author or admin can modify post");
        }
    }

    private List<PostStatus> normalizeStatuses(List<PostStatus> input) {
        if (input == null || input.isEmpty()) {
            return Arrays.asList(PostStatus.values());
        }
        return input;
    }
}
