package com.msa.board.web;

import com.msa.board.application.command.SearchPostsCommand;
import com.msa.board.application.port.PostUseCase;
import com.msa.board.domain.PostStatus;
import com.msa.board.web.request.CreatePostRequest;
import com.msa.board.web.request.UpdatePostRequest;
import com.msa.board.web.response.PageResponse;
import com.msa.board.web.response.PostResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/posts")
public class PostController {

    private final PostUseCase postUseCase;

    public PostController(PostUseCase postUseCase) {
        this.postUseCase = postUseCase;
    }

    @PostMapping
    public PostResponse createPost(
            @PathVariable UUID boardId,
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication) {
        String requester = authentication.getName();
        return PostResponse.from(postUseCase.createPost(request.toCommand(boardId, requester)));
    }

    @GetMapping
    public PageResponse<PostResponse> listPosts(
            @PathVariable UUID boardId,
            @RequestParam(name = "status", required = false) List<String> statuses,
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostStatus> statusEnums = toPostStatuses(statuses);
        Page<PostResponse> result = postUseCase.searchPosts(
                        new SearchPostsCommand(boardId, author, statusEnums, page, size))
                .map(PostResponse::from);
        return PageResponse.from(result);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable UUID boardId, @PathVariable UUID postId) {
        return PostResponse.from(postUseCase.getPost(boardId, postId));
    }

    @PatchMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable UUID boardId,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication) {
        String requester = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return PostResponse.from(postUseCase.updatePost(
                request.toCommand(boardId, postId, requester, isAdmin)));
    }

    @DeleteMapping("/{postId}")
    public void archivePost(
            @PathVariable UUID boardId,
            @PathVariable UUID postId,
            Authentication authentication) {
        String requester = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postUseCase.archivePost(boardId, postId, requester, isAdmin);
    }

    private List<PostStatus> toPostStatuses(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(v -> PostStatus.valueOf(v.toUpperCase())).toList();
    }
}
