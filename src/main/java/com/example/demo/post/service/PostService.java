package com.example.demo.post.service;

import com.example.demo.post.dto.DeletePostResponse;
import com.example.demo.post.dto.GetPostResponse;
import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.post.dto.*;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostSort;
import com.example.demo.post.enums.PostStatus;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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

        boolean boardExists = boardRepository.existsById(boardId);
        if (!boardExists){
            throw new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.BOARD_NOT_FOUND);
        }

        // 1-based -> 0-based page index
        int page = Math.max(0, request.getPage() - 1);
        int pageSize = request.getPageSize();
        PostSort postSort = request.getSort();


        Sort sort;
        switch (postSort) {
            case LATEST:
                sort = Sort.by("createdAt").descending();
                break;
            case HOT:
                sort = Sort.by("hotScore").descending();
                break;
            default:
                sort = Sort.by("createdAt").descending();
        }


        Pageable pageable = PageRequest.of(page, pageSize, sort);

        // 取得分頁內容
        Page<PostItem> pageResult = postRepository.findByBoardId(boardId, PostStatus.ACTIVE, pageable);

        ListPostsResponse response = new ListPostsResponse();
        response.setPage(pageResult.getNumber() + 1);
        response.setPageSize(pageResult.getSize());
        response.setTotal(pageResult.getTotalElements());
        response.setItems(pageResult.getContent());
        return response;
    }

    @Transactional(readOnly = true)
    public GetPostResponse getPost(long postId) {
        // 查詢狀態為 ACTIVE 的文章
        Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.POST_NOT_FOUND));

        // 構建回應
        GetPostResponse response = new GetPostResponse();
        response.setPostId(post.getPostId());
        response.setBoardId(post.getBoard().getBoardId());
        response.setBoardName(post.getBoard().getName());
        response.setAuthorId(post.getAuthor().getUserId());
        response.setAuthorName(post.getAuthor().getDisplayName());
        response.setTitle(post.getTitle());
        response.setBody(post.getBody());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }

    @Transactional
    public DeletePostResponse deletePost(long postId, User currentUser) {
        // 查詢文章是否存在
        Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.POST_NOT_FOUND));

        //判斷是否為作者本人或管理員
        boolean isAuthor = post.getAuthor().getUserId() == currentUser.getUserId();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ApiException(ErrorMessage.FORBIDDEN, PostErrorCode.NOT_POST_AUTHOR);
        }

        // 更新文章狀態為 DELETED
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);

        // 返回刪除成功的回應
        DeletePostResponse response = new DeletePostResponse();
        response.setPostId(postId);
        response.setStatus(PostStatus.DELETED);
        return response;
    }

    @Transactional
    public UpdatePostResponse updatePost(long postId, User currentUser, UpdatePostRequest request) {
        // 查詢文章是否存在
        Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.POST_NOT_FOUND));

        //判斷是否為作者本人或管理員
        boolean isAuthor = post.getAuthor().getUserId() == currentUser.getUserId();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ApiException(ErrorMessage.FORBIDDEN, PostErrorCode.NOT_POST_AUTHOR);
        }

        // 更新文章標題和內容（如果提供）
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }

        post.setUpdatedAt(Instant.now());

        // 保存更新後的文章
        postRepository.save(post);

        // 返回更新成功的回應
        UpdatePostResponse response = new UpdatePostResponse();
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setBody(post.getBody());
        return response;
    }
}
