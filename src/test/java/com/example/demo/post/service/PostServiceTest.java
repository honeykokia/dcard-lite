package com.example.demo.post.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostService postService;

    @Test
    void createPost_Success() {
        // == Given ==
        long boardId = 2L;
        long userId = 1L;
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("關於SpringBoot");
        request.setBody("內容...");

        // 1. 模擬看板存在
        Board mockBoard = new Board();
        mockBoard.setBoardId(boardId);
        mockBoard.setName("軟體版");

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setEmail("leo@example.com");
        mockUser.setDisplayName("leo");
        mockUser.setPasswordHash("abc12345");
        mockUser.setRole(UserRole.USER);
        mockUser.setCreatedAt(Instant.now());

        // 模擬 Repository 找得到看板
        given(boardRepository.findById(boardId)).willReturn(Optional.of(mockBoard));
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        Post savedPost = new Post();
        savedPost.setPostId(100L);

        // 2. 模擬存檔行為 (這裡我們設定：存什麼就回傳什麼)
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // == When ==
        CreatePostResponse response = postService.createPost(boardId, userId, request);

        // == Then ==
        assertEquals(response.getPostId(), 100L);

        // 這就是你計畫中提到的「驗證真的存對看板、驗證作者是對的」
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture()); // 抓取參數

        Post post = postCaptor.getValue(); // 取得抓到的物件

        assertEquals(post.getBoard().getBoardId(),boardId);
        assertEquals(post.getAuthor().getUserId(),userId);
        assertEquals(post.getTitle(),"關於SpringBoot");
        assertEquals(post.getStatus(),PostStatus.ACTIVE);
    }

    @Test
    void createPost_BoardNotFound_ThrowException() {
        // == Given ==
        long nonExistBoardId = 3L;
        long userId = 1L;
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");

        given(boardRepository.findById(nonExistBoardId)).willReturn(Optional.empty());
        // == When ==
        // 使用你喜歡的 assertThrows，把抓到的例外存進變數
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.createPost(nonExistBoardId, userId, mockRequest);
        });

        // == Then ==
        // 這裡使用 assertEquals 來檢查細節
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.BOARD_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(boardRepository).findById(nonExistBoardId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_UserNotFound_ThrowException() {
        // == Given ==
        long boardId = 2L;
        long nonExistUserId = 99L;
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");

        Board mockBoard = new Board();
        mockBoard.setBoardId(boardId);
        mockBoard.setName("軟體版");

        given(boardRepository.findById(boardId)).willReturn(Optional.of(mockBoard));
        given(userRepository.findById(nonExistUserId)).willReturn(Optional.empty());

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.createPost(boardId, nonExistUserId, mockRequest);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(boardRepository).findById(boardId);
        verify(userRepository).findById(nonExistUserId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void listPosts_Success() {
        // == Given ==
        long boardId = 2L;

        ListPostsRequest mockRequest = new ListPostsRequest();
        mockRequest.setPage(1);
        mockRequest.setPageSize(40);
        mockRequest.setSort(PostSort.LATEST);

        Board mockBoard = new Board();
        mockBoard.setBoardId(boardId);
        mockBoard.setName("軟體版");
        mockBoard.setDescription("聊軟體相關的知識");
        mockBoard.setCreatedAt(Instant.now());

        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setDisplayName("Leo");
        mockUser.setRole(UserRole.USER);

        PostItem postItem1 = new PostItem();
        postItem1.setPostId(1L);
        postItem1.setBoardId(boardId);
        postItem1.setBoardName("軟體版");
        postItem1.setAuthorId(1L);
        postItem1.setAuthorName("Leo");
        postItem1.setTitle("關於SpringBoot的問題");
        postItem1.setLikeCount(0);
        postItem1.setHotScore(0.0);
        postItem1.setStatus(PostStatus.ACTIVE);
        postItem1.setCreatedAt(LocalDateTime.of(2025,12,25,10,0,0).toInstant(ZoneOffset.UTC));

        PostItem postItem2 = new PostItem();
        postItem2.setPostId(2L);
        postItem2.setBoardId(boardId);
        postItem2.setBoardName("軟體版");
        postItem2.setAuthorId(1L);
        postItem2.setAuthorName("Leo");
        postItem2.setTitle("關於JAVA問題?");
        postItem2.setLikeCount(0);
        postItem2.setHotScore(0.0);
        postItem2.setStatus(PostStatus.ACTIVE);
        postItem2.setCreatedAt(LocalDateTime.of(2026,1,1,12,0,0).toInstant(ZoneOffset.UTC));

        Pageable mockPageable = PageRequest.of(0, 40, Sort.by("createdAt").descending());
        Page<PostItem> mockPage = new PageImpl<>(List.of(postItem1, postItem2), mockPageable, 2);
        given(boardRepository.existsById(boardId)).willReturn(true);
        given(postRepository.findByBoardId(eq(boardId),eq(PostStatus.ACTIVE), any(Pageable.class))).willReturn(mockPage);

        // == When ==
        ListPostsResponse response = postService.listPosts(boardId, mockRequest);

        // == Then ==
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findByBoardId(eq(boardId), eq(PostStatus.ACTIVE), pageCaptor.capture());
        Pageable capturedPageable = pageCaptor.getValue();
        assertEquals(0,capturedPageable.getPageNumber());
        assertEquals(40,capturedPageable.getPageSize());
        assertEquals(Sort.by("createdAt").descending(),capturedPageable.getSort());
        assertEquals(response.getItems().size(),2);

        verify(boardRepository).existsById(boardId);
        verify(postRepository).findByBoardId(eq(boardId), eq(PostStatus.ACTIVE), any(Pageable.class));

    }

    @Test
    void listPosts_HotSort_Success() {
        // == Given ==
        long boardId = 2L;

        ListPostsRequest mockRequest = new ListPostsRequest();
        mockRequest.setPage(1);
        mockRequest.setPageSize(20);
        mockRequest.setSort(PostSort.HOT);

        Board mockBoard = new Board();
        mockBoard.setBoardId(boardId);
        mockBoard.setName("軟體版");
        mockBoard.setDescription("聊軟體相關的知識");
        mockBoard.setCreatedAt(Instant.now());

        given(boardRepository.existsById(boardId)).willReturn(true);

        Pageable mockPageable = PageRequest.of(0, 20, Sort.by("hotScore").descending());
        Page<PostItem> mockPage = new PageImpl<>(List.of(), mockPageable, 0);
        given(postRepository.findByBoardId(eq(boardId), eq(PostStatus.ACTIVE), any(Pageable.class))).willReturn(mockPage);

        // == When ==
        ListPostsResponse response = postService.listPosts(boardId, mockRequest);

        // == Then ==
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findByBoardId(eq(boardId), eq(PostStatus.ACTIVE), pageCaptor.capture());
        Pageable capturedPageable = pageCaptor.getValue();
        assertEquals(0,capturedPageable.getPageNumber());
        assertEquals(20,capturedPageable.getPageSize());
        assertEquals(Sort.by("hotScore").descending(),capturedPageable.getSort());
        assertEquals(response.getItems().size(),0);

        verify(boardRepository).existsById(boardId);
        verify(postRepository).findByBoardId(eq(boardId), eq(PostStatus.ACTIVE), any(Pageable.class));

    }

    @Test
    void listPosts_BoardNotFound_ThrowException() {
        // == Given ==
        long nonExistBoardId = 3L;
        ListPostsRequest mockRequest = new ListPostsRequest();
        mockRequest.setPage(1);
        mockRequest.setPageSize(20);
        mockRequest.setSort(PostSort.LATEST);

        given(boardRepository.existsById(nonExistBoardId)).willReturn(false);

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.listPosts(nonExistBoardId, mockRequest);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.BOARD_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(boardRepository).existsById(nonExistBoardId);
        verify(postRepository, never()).findByBoardId(anyLong(), any(PostStatus.class), any(Pageable.class));
    }

    @Test
    void getPost_Success(){
        // == Given ==
        long postId = 1L;

        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setDisplayName("Leo");
        mockUser.setRole(UserRole.USER);

        Board mockBoard = new Board();
        mockBoard.setBoardId(2L);
        mockBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockBoard);
        mockPost.setAuthor(mockUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        given(postRepository.findByPostIdAndStatus(postId,PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        GetPostResponse response = postService.getPost(postId);

        // == Then ==
        assertEquals(response.getPostId(),mockPost.getPostId());
        assertEquals(response.getBoardId(),mockBoard.getBoardId());
        assertEquals(response.getBoardName(),mockBoard.getName());
        assertEquals(response.getAuthorId(),mockUser.getUserId());
        assertEquals(response.getAuthorName(),mockUser.getDisplayName());
        assertEquals(response.getTitle(),mockPost.getTitle());
        assertEquals(response.getBody(),mockPost.getBody());
        assertEquals(response.getLikeCount(),mockPost.getLikeCount());
        assertEquals(response.getCommentCount(),mockPost.getCommentCount());

        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
    }

    @Test
    void getPost_PostNotFoundOrDeleted_ThrowException() {
        // == Given ==
        long targetPostId = 99L;

        given(postRepository.findByPostIdAndStatus(targetPostId, PostStatus.ACTIVE))
                .willReturn(Optional.empty());

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.getPost(targetPostId);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(postRepository).findByPostIdAndStatus(targetPostId, PostStatus.ACTIVE);
    }

    @Test
    void deletePost_Success(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(mockPostUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        postService.deletePost(postId, mockPostUser);

        //== Then ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertEquals(savedPost.getStatus(),PostStatus.DELETED);
    }

    @Test
    void deletePost_NotPostAuthor_ThrowException(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(99L); // 非作者
        mockCurrentUser.setDisplayName("NotLeo");
        mockCurrentUser.setRole(UserRole.USER);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(mockPostUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.deletePost(postId, mockCurrentUser); // 使用非作者的 userId
        });

        // == Then ==
        assertEquals(ErrorMessage.FORBIDDEN, exception.getErrorMessage());
        assertEquals(PostErrorCode.NOT_POST_AUTHOR, exception.getErrorCode());

        // == Verify ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_UserIsAdmin_Success(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(99L); // 非作者
        mockCurrentUser.setDisplayName("Admin");
        mockCurrentUser.setRole(UserRole.ADMIN);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(new User()); // 文章作者不是 Admin
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        postService.deletePost(postId, mockCurrentUser);

        //== Then ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertEquals(savedPost.getStatus(),PostStatus.DELETED);
    }

    @Test
    void deletePost_PostNotFound_ThrowException(){
        // == Given ==
        long nonExistPostId = 99L;
        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(1L);
        mockCurrentUser.setDisplayName("Leo");
        mockCurrentUser.setRole(UserRole.USER);

        given(postRepository.findByPostIdAndStatus(nonExistPostId, PostStatus.ACTIVE)).willReturn(Optional.empty());

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.deletePost(nonExistPostId, mockCurrentUser);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(postRepository).findByPostIdAndStatus(nonExistPostId, PostStatus.ACTIVE);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_Success(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(mockPostUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setBody("更新後的內容");

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        postService.updatePost(postId, mockPostUser, updateRequest);

        //== Then ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertEquals(savedPost.getTitle(),updateRequest.getTitle());
        assertEquals(savedPost.getBody(),updateRequest.getBody());
    }
    @Test
    void updatePost_OnlyUpdateTitle_Success(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(mockPostUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setBody(null); // 只更新標題

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        postService.updatePost(postId, mockPostUser, updateRequest);

        //== Then ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertEquals(savedPost.getTitle(),updateRequest.getTitle());
        assertEquals(savedPost.getBody(),mockPost.getBody()); // 內容保持不變
    }

    @Test
    void updatePost_UserIsAdmin_Success(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(99L); // 非作者
        mockCurrentUser.setDisplayName("Admin");
        mockCurrentUser.setRole(UserRole.ADMIN);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(new User()); // 文章作者不是 Admin
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setBody("更新後的內容");

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        postService.updatePost(postId, mockCurrentUser, updateRequest);

        //== Then ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertEquals(savedPost.getTitle(),updateRequest.getTitle());
        assertEquals(savedPost.getBody(),updateRequest.getBody());
    }

    @Test
    void updatePost_NotPostAuthor_ThrowException(){
        // == Given ==
        long postId = 1L;

        User mockPostUser = new User();
        mockPostUser.setUserId(1L);
        mockPostUser.setDisplayName("Leo");
        mockPostUser.setRole(UserRole.USER);

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(99L); // 非作者
        mockCurrentUser.setDisplayName("NotLeo");
        mockCurrentUser.setRole(UserRole.USER);

        Board mockPostBoard = new Board();
        mockPostBoard.setBoardId(2L);
        mockPostBoard.setName("軟體版");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setBoard(mockPostBoard);
        mockPost.setAuthor(mockPostUser);
        mockPost.setTitle("關於SpringBoot的問題");
        mockPost.setBody("請問如何創建專案?");
        mockPost.setLikeCount(0);
        mockPost.setCommentCount(0);
        mockPost.setStatus(PostStatus.ACTIVE);
        mockPost.setCreatedAt(Instant.now());

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setBody("更新後的內容");

        given(postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.updatePost(postId, mockCurrentUser, updateRequest); // 使用非作者的 userId
        });

        // == Then ==
        assertEquals(ErrorMessage.FORBIDDEN, exception.getErrorMessage());
        assertEquals(PostErrorCode.NOT_POST_AUTHOR, exception.getErrorCode());

        // == Verify ==
        verify(postRepository).findByPostIdAndStatus(postId,PostStatus.ACTIVE);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_PostNotFound_ThrowException(){
        // == Given ==
        long nonExistPostId = 3L;
        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(1L);
        mockCurrentUser.setDisplayName("Leo");
        mockCurrentUser.setRole(UserRole.USER);

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setBody("更新後的內容");

        given(postRepository.findByPostIdAndStatus(nonExistPostId, PostStatus.ACTIVE)).willReturn(Optional.empty());

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () -> {
            postService.updatePost(nonExistPostId, mockCurrentUser, updateRequest);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND, exception.getErrorMessage());
        assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());

        // == Verify ==
        verify(postRepository).findByPostIdAndStatus(nonExistPostId,PostStatus.ACTIVE);
        verify(postRepository, never()).save(any());
    }
}
