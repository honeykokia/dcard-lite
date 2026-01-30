package com.example.demo.post.controller;

import com.example.demo.post.dto.GetPostResponse;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtAuthenticationFilter;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}
