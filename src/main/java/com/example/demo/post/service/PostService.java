package com.example.demo.post.service;

import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.post.dto.CreatePostRequest;
import com.example.demo.post.dto.CreatePostResponse;
import com.example.demo.post.entity.Post;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, BoardRepository boardRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
    }

    public CreatePostResponse createPost(
            long boardId,
            long userId,
            CreatePostRequest request) {

        // 驗證看板是否存在
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.BOARD_NOT_FOUND));

        // 驗證作者是否存在
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.USER_NOT_FOUND));

        // 創建 Post 實體
        Post post = new Post();
        post.setBoard(board);
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        post.setStatus("ACTIVE");

        // 保存 Post
        Post savedPost = postRepository.save(post);

        // 返回 Response
        CreatePostResponse response = new CreatePostResponse();
        response.setPostId(savedPost.getPostId());
        return response;
    }
}
