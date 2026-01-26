package com.example.demo.post.service;

import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.post.dto.CreatePostRequest;
import com.example.demo.post.dto.CreatePostResponse;
import com.example.demo.post.dto.ListPostsRequest;
import com.example.demo.post.dto.ListPostsResponse;
import com.example.demo.post.dto.PostItem;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostSort;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Comparator;

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

    @Transactional
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

        // 保存 Post
        Post savedPost = postRepository.save(post);

        // 返回 Response
        CreatePostResponse response = new CreatePostResponse();
        response.setPostId(savedPost.getPostId());
        return response;
    }

    @Transactional(readOnly = true)
    public ListPostsResponse listPosts(long boardId, ListPostsRequest request) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.BOARD_NOT_FOUND));

        // 1-based -> 0-based page index
        int page = Math.max(0, request.getPage() - 1);
        int pageSize = request.getPageSize();
        PostSort postSort = request.getSort();


        Sort sort;
        switch (postSort) {
            case LATEST:
                sort = Sort.by("p.createdAt").descending();
                break;
            case HOT:
                sort = Sort.by("p.hotScore").descending();
                break;
            default:
                sort = Sort.by("p.createdAt").descending();
        }


        Pageable pageable = PageRequest.of(page, pageSize, sort);

        // 取得分頁內容
        Page<PostItem> pageResult = postRepository.findByBoardId(boardId, pageable);

        ListPostsResponse response = new ListPostsResponse();
        response.setPage(pageResult.getNumber() + 1);
        response.setPageSize(pageResult.getSize());
        response.setTotal(pageResult.getTotalElements());
        response.setItems(pageResult.getContent());
        return response;
    }
}
