package com.msa.board.application.port;

import com.msa.board.application.command.CreatePostCommand;
import com.msa.board.application.command.SearchPostsCommand;
import com.msa.board.application.command.UpdatePostCommand;
import com.msa.board.domain.Post;
import org.springframework.data.domain.Page;

public interface PostUseCase {
    Post createPost(CreatePostCommand command);

    Post updatePost(UpdatePostCommand command);

    void archivePost(java.util.UUID boardId, java.util.UUID postId, String requesterEmail, boolean isAdmin);

    Post getPost(java.util.UUID boardId, java.util.UUID postId);

    Page<Post> searchPosts(SearchPostsCommand command);
}
