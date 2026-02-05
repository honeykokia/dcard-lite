package com.example.demo.post.controller;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtAuthenticationFilter;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.example.demo.post.dto.DeletePostResponse;
import com.example.demo.post.dto.GetPostResponse;
import com.example.demo.post.dto.UpdatePostRequest;
import com.example.demo.post.dto.UpdatePostResponse;
import com.example.demo.post.enums.PostStatus;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.service.PostService;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class})
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PostService postService;


    @Test
    void getPost_success() throws Exception {
        // == Given ==
        long postId = 1L;

        GetPostResponse mockResponse = GetPostResponse.builder()
                .postId(postId)
                .authorId(1L)
                .authorName("Leo")
                .boardId(2L)
                .boardName("軟體版")
                .title("關於SpringBoot問題")
                .body("請問如何創建專案?")
                .likeCount(0)
                .commentCount(0)
                .createdAt(Instant.now()).build();

        given(postService.getPost(postId)).willReturn(mockResponse);

        // == When ==
        ResultActions result = mockMvc.perform(get("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON));


        // == Then ==
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.authorId").value(1L))
                .andExpect(jsonPath("$.authorName").value("Leo"))
                .andExpect(jsonPath("$.boardId").value(2L))
                .andExpect(jsonPath("$.boardName").value("軟體版"))
                .andExpect(jsonPath("$.title").value("關於SpringBoot問題"))
                .andExpect(jsonPath("$.body").value("請問如何創建專案?"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0));


    }

    @ValueSource(strings = {
            "0",
            "-1",
            "abc"
    })
    @ParameterizedTest
    void getPost_PathInvalid_Return400(String invalidPostId) throws Exception {
        // == When & Then ==
        mockMvc.perform(get("/posts/{postId}", invalidPostId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.PATH_FORMAT_ERROR.name()));


    }

    @Test
    void getPost_PostNotFound_Return404() throws Exception {
        // == Given ==
        long postId = 99L;
        given(postService.getPost(postId))
                .willThrow(new ApiException(
                        ErrorMessage.NOT_FOUND,
                        PostErrorCode.POST_NOT_FOUND
                ));

        // == When & Then ==
        mockMvc.perform(get("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.POST_NOT_FOUND.name()));
    }

    @Test
    void deletePost_success() throws Exception {

        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        long postId = 1L;

        DeletePostResponse mockResponse = DeletePostResponse.builder()
                .postId(postId)
                .status(PostStatus.DELETED)
                .build();

        given(postService.deletePost(eq(postId), eq(mockUser)))
                .willReturn(mockResponse);

        // == When ==
        ResultActions result = mockMvc.perform(delete("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))); // 模擬已登入的使用者

        // == Then ==
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.status").value(PostStatus.DELETED.name()));

        // == Verify ==
        verify(postService,times(1)).deletePost(eq(postId), eq(mockUser));

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "-1",
            "abc"
    })
    void deletePost_PathInvalid_Return400(String invalidPostId) throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        // == When & Then ==
        mockMvc.perform(delete("/posts/{postId}", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.PATH_FORMAT_ERROR.name()));
    }

    @Test
    void deletePost_Unauthorized_Return401() throws Exception {
        // == Given ==
        long postId = 1L;

        // == When & Then ==
        mockMvc.perform(delete("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.SECURITY_UNAUTHORIZED.name()));
    }

    @Test
    void deletePost_NotPostAuthor_Return403() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 1L; // 假設文章 ID 是 1 (不屬於 User 2)

        ApiException expectedException = new ApiException(
                ErrorMessage.FORBIDDEN,
                PostErrorCode.NOT_POST_AUTHOR
        );

        given(postService.deletePost(eq(postId), eq(mockUser)))
                .willThrow(expectedException);

        // == When ==
        ResultActions result = mockMvc.perform(delete("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)));

        // == Then ==
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorMessage.FORBIDDEN.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.NOT_POST_AUTHOR.name()));

    }

    @Test
    void deletePost_PostNotFound_Return404() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 3L; // DB不存在 的文章 ID

        ApiException expectedException = new ApiException(
                ErrorMessage.NOT_FOUND,
                PostErrorCode.POST_NOT_FOUND
        );

        given(postService.deletePost(eq(postId), eq(mockUser)))
                .willThrow(expectedException);

        // == When ==
        ResultActions result = mockMvc.perform(delete("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)));

        // == Then ==
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.POST_NOT_FOUND.name()));
    }

    @Test
    void updatePost_success() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 1L;

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody("更新後的內容");

        UpdatePostResponse mockResponse = new UpdatePostResponse();
        mockResponse.setPostId(postId);
        mockResponse.setTitle(mockRequest.getTitle());
        mockResponse.setBody(mockRequest.getBody());

        given(postService.updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        )).willReturn(mockResponse);

        // == When ==
        ResultActions result = mockMvc.perform(patch("/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser))
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.title").value(mockRequest.getTitle()))
                .andExpect(jsonPath("$.body").value(mockRequest.getBody()));

        // == Verify ==
        verify(postService, times(1)).updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        );

    }

    @Test
    void updatePost_OnlyTitleUpdated_success() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 1L;

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody(null);

        UpdatePostResponse mockResponse = new UpdatePostResponse();
        mockResponse.setPostId(postId);
        mockResponse.setTitle(mockRequest.getTitle());
        mockResponse.setBody(mockRequest.getBody());

        given(postService.updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        )).willReturn(mockResponse);


        // == When ==
        ResultActions result = mockMvc.perform(patch("/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser))
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.title").value(mockRequest.getTitle()))
                .andExpect(jsonPath("$.body").value(mockRequest.getBody()));
        // == Verify ==
        verify(postService, times(1)).updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "-1",
            "abc"
    })
    void updatePost_PathInvalid_Return400(String invalidPostId) throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody("更新後的內容");

        // == When & Then ==
        mockMvc.perform(patch("/posts/{postId}", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.PATH_FORMAT_ERROR.name()));


    }

    static Stream<Arguments> provideInvalidTitles() {
        return Stream.of(
                // 情境 1: 空字串
                Arguments.of(""),
                // 情境 2: 全空白
                Arguments.of("    "),
                // 情境 3: 超過 50 字 (51個a)
                Arguments.of("a".repeat(51)),
                // 情境 5: 包含 < >
                Arguments.of("<script>alert('hack')</script>"));
    }
    @ParameterizedTest(name = "標題測試: title={0} 應回傳 400")
    @MethodSource("provideInvalidTitles")
    void updatePost_InvalidTitle_Return400(String invalidTitle) throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle(invalidTitle);
        mockRequest.setBody("更新後的內容");

        // == When & Then ==
        mockMvc.perform(patch("/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.TITLE_INVALID.name()));
    }

    static Stream<Arguments> provideInvalidBodies() {
        return Stream.of(
                // 情境 1: 空字串
                Arguments.of(""),
                // 情境 2: 全空白
                Arguments.of("    "),
                // 情境 3: 超過 300 字 (301個a)
                Arguments.of("a".repeat(301)),
                // 情境 5: 包含 < >
                Arguments.of("<script>alert('hack')</script>"));
    }
    @ParameterizedTest(name = "內容測試: body={0} 應回傳 400")
    @MethodSource("provideInvalidBodies")
    void updatePost_InvalidBody_Return400(String invalidBody) throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("想詢問Java問題");
        mockRequest.setBody(invalidBody);

        // == When & Then ==
        mockMvc.perform(patch("/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.BODY_INVALID.name()));

    }

    @Test
    void updatePost_Unauthorized_Return401() throws Exception {
        // == Given ==
        long postId = 1L;

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody("更新後的內容");

        // == When & Then ==
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.SECURITY_UNAUTHORIZED.name()));
    }

    @Test
    void updatePost_NotPostAuthor_Return403() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 1L; // 假設文章 ID 是 1 (不屬於 User 2)

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody("更新後的內容");

        ApiException expectedException = new ApiException(
                ErrorMessage.FORBIDDEN,
                PostErrorCode.NOT_POST_AUTHOR
        );

        given(postService.updatePost(eq(postId), eq(mockUser), refEq(mockRequest)))
                .willThrow(expectedException);

        // == When ==
        ResultActions result = mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))
                        .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorMessage.FORBIDDEN.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.NOT_POST_AUTHOR.name()));

        // == Verify ==
        verify(postService, times(1)).updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        );

    }

    @Test
    void updatePost_PostNotFound_Return404() throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        long postId = 3L; // DB不存在 的文章 ID

        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("更新後的標題");
        mockRequest.setBody("更新後的內容");

        ApiException expectedException = new ApiException(
                ErrorMessage.NOT_FOUND,
                PostErrorCode.POST_NOT_FOUND
        );

        given(postService.updatePost(eq(postId), eq(mockUser), refEq(mockRequest)))
                .willThrow(expectedException);

        // == When ==
        ResultActions result = mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser))
                        .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.POST_NOT_FOUND.name()));

        // == Verify ==
        verify(postService, times(1)).updatePost(
                eq(postId),
                eq(mockUser),
                refEq(mockRequest)
        );
    }
}
