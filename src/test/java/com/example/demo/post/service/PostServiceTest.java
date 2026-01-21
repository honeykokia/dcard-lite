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
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        assertEquals(response.getPostId(),100L);

        // 這就是你計畫中提到的「驗證真的存對看板、驗證作者是對的」
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture()); // 抓取參數

        Post post = postCaptor.getValue(); // 取得抓到的物件

        assertEquals(post.getBoard().getBoardId(),boardId);
        assertEquals(post.getAuthor().getUserId(),userId);
        assertEquals(post.getTitle(),"關於SpringBoot");
        assertEquals(post.getStatus(),"ACTIVE");
    }

    @Test
    void createPost_BoardNotFound_ThrowException() {
        // == Given ==
        long nonExistBoardId = 3L;
        long userId = 1L;
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Mavan專案");

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
        mockRequest.setBody("請問怎麼建立一個SpringBoot Mavan專案");

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
}
