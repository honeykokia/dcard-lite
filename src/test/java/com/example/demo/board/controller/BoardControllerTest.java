package com.example.demo.board.controller;

import com.example.demo.board.dto.BoardItem;
import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.error.BoardErrorCode;
import com.example.demo.board.service.BoardService;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtAuthenticationFilter;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.example.demo.post.dto.CreatePostRequest;
import com.example.demo.post.dto.CreatePostResponse;
import com.example.demo.post.error.PostErrorCode;
import com.example.demo.post.service.PostService;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BoardController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class})
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static BoardItem createBoardItem(Long boardId, String name, String description){
        BoardItem item = new BoardItem();
        item.setBoardId(boardId);
        item.setName(name);
        item.setDescription(description);
        return item;
    }

    @Test
    void listBoards_Success() throws Exception {
        // == Given ==
        BoardItem item = createBoardItem(1L, "測試版", "Desc");
        ListBoardsResponse mockResponse = new ListBoardsResponse();
        mockResponse.setItems(List.of(item));
        mockResponse.setTotal(1L);
        mockResponse.setPage(1);
        mockResponse.setPageSize(20);

        // 這裡用 any() 沒關係，因為我們只在乎 Response 結構
        given(boardService.listBoards(any())).willReturn(mockResponse);

        // == When & Then ==
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].name").value("測試版"));
    }
    @Test
    void listBoards_WithKeyword_ShouldPassParamToService() throws Exception {
        // == Given ==
        given(boardService.listBoards(any())).willReturn(new ListBoardsResponse());

        // == When ==
        mockMvc.perform(get("/boards")
                .param("keyword", "特定關鍵字"));


        // == Then ==
        // 使用 ArgumentCaptor 來抓取 Controller 傳給 Service 的參數
        ArgumentCaptor<ListBoardsRequest> captor = ArgumentCaptor.forClass(ListBoardsRequest.class);

        verify(boardService).listBoards(captor.capture());

        // 驗證：Controller 真的有把 "特定關鍵字" 塞進去 Request 物件嗎？
        assertEquals("特定關鍵字", captor.getValue().getKeyword());
    }

    @Test
    void listBoards_ValidKeywordOnBoundary_Return200() throws Exception {
        String boundaryKeyword = " " + "a".repeat(50) + " ";

        // 2. 執行測試
        mockMvc.perform(get("/boards")
                        .param("keyword", boundaryKeyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "測試案例: page={0}, 預期錯誤={1}")
    @CsvSource({
            "0,      PAGE_INVALID",
            "-1,     PAGE_INVALID",
            "invalid,               PARAM_FORMAT_ERROR"
    })
    void listBoards_PageInvalid_Return400(String page, String expectedErrorCode) throws Exception{
        // == When & Then ==
        mockMvc.perform(get("/boards")
                .param("page",page) // Invalid page
                .param("pageSize","20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(expectedErrorCode));
    }


    @ParameterizedTest(name = "測試案例: pageSize={0}, 預期錯誤={1}")
    @CsvSource({
            "0,      PAGE_SIZE_INVALID",
            "101,     PAGE_SIZE_INVALID",
            "invalid,               PARAM_FORMAT_ERROR"
    })
    void listBoards_PageSizeInvalid_Return400(String pageSize, String expectedErrorCode) throws Exception{
        // == When & Then ==
        mockMvc.perform(get("/boards")
                        .param("page","1") // Valid page (fixed), testing pageSize validation
                        .param("pageSize",pageSize)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(expectedErrorCode));
    }


    static Stream<String> provideInvalidKeywords() {
        return Stream.of(
                // 情境 1: 純粹超過 50 字 (51個a)
                "a".repeat(51),
                // 情境 2: 驗證 trim() 後長度依然超過 (符合 spec 定義的邊界測試)
                " " + "a".repeat(51) + " "
        );
    }
    @ParameterizedTest
    @MethodSource("provideInvalidKeywords")
    void listBoards_keywordInvalid_Return400(String keywordInput) throws Exception{
        // == When & Then ==
        mockMvc.perform(get("/boards")
                        .param("page","1")
                        .param("pageSize","20")
                        .param("keyword",keywordInput)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(BoardErrorCode.KEYWORD_INVALID.name()));
    }

    @Test
    void createPost_Success() throws Exception {
        // == Given ==
        long boardId = 2L;
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");


        CreatePostResponse mockResponse = new CreatePostResponse();
        mockResponse.setPostId(1L);
        given(postService.createPost(eq(boardId), anyLong(), any(CreatePostRequest.class)
        )).willReturn(mockResponse);

        // == When ==
        ResultActions performResult = mockMvc.perform(post("/boards/{boardId}/posts", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser)) // 模擬已登入的使用者
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        performResult
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(1L));

        verify(postService).createPost(
                eq(boardId),
                eq(mockUser.getUserId()),
                refEq(mockRequest) // 檢查傳進去的物件內容是不是我們寫的那樣
        );

    }

    static Stream<Arguments> provideInvalidTitles() {
        return Stream.of(
                // 情境 1: 空字串
                Arguments.of(""),
                // 情境 2: 全空白
                Arguments.of("    "),
                // 情境 3: 超過 50 字 (51個a)
                Arguments.of("a".repeat(51)),
                // 情境 4: null
                Arguments.of((String) null),
                // 情境 5: 包含 < >
                Arguments.of("<script>alert('hack')</script>")
        );
    }
    @ParameterizedTest(name = "標題測試: title={0} 應回傳 400")
    @MethodSource("provideInvalidTitles")
    void createPost_TitleInvalid_Return400(String title) throws Exception {
        // == Given ==
        long boardId = 2L;
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle(title);
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");

        // == When ==
        ResultActions performResult = mockMvc.perform(post("/boards/{boardId}/posts", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser)) // 模擬已登入的使用者
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        performResult
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.TITLE_INVALID.name()));
    }

    static Stream<Arguments> provideInvalidBodys() {
        return Stream.of(
                // 情境 1: 空字串
                Arguments.of(""),
                // 情境 2: 全空白
                Arguments.of("    "),
                // 情境 3: 超過 300 字 (301個a)
                Arguments.of("a".repeat(301)),
                // 情境 4: null
                Arguments.of((String) null),
                // 情境 5: 包含 < >
                Arguments.of("<script>alert('hack')</script>")
        );
    }
    @ParameterizedTest(name = "內容測試: body={0} 應回傳 400")
    @MethodSource("provideInvalidBodys")
    void createPost_BodyInvalid_Return400(String body) throws Exception {
        // == Given ==
        long boardId = 2L;
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody(body);

        // == When ==
        ResultActions performResult = mockMvc.perform(post("/boards/{boardId}/posts", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser)) // 模擬已登入的使用者
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        performResult
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.BODY_INVALID.name()));
    }

    @ValueSource(strings = {
            "abc",
            "-1",
    })
    @ParameterizedTest
    void createPost_PathInvalid_Return400(String invalidBoardId) throws Exception {
        // == Given ==
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");

        // == When ==
        ResultActions performResult = mockMvc.perform(post("/boards/{boardId}/posts", invalidBoardId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser)) // 模擬已登入的使用者
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        performResult
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(PostErrorCode.PATH_FORMAT_ERROR.name()));
    }

    @Test
    void createPost_NoToken_Return401() throws Exception {
        // == Given ==
        long boardId = 2L;
        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Mavan專案");

        // == When ==
        ResultActions result = mockMvc.perform(post("/boards/{boardId}/posts", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(PostErrorCode.SECURITY_UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.name()));
    }

    @Test
    void createPost_NotFound_Return404() throws Exception {
        // == Given ==
        long nonExistBoardId = 3L;
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("leo@example.com");
        mockUser.setPasswordHash("pass");
        mockUser.setRole(UserRole.USER);

        CreatePostRequest mockRequest = new CreatePostRequest();
        mockRequest.setTitle("關於SpringBoot的問題");
        mockRequest.setBody("請問怎麼建立一個SpringBoot Maven專案");

        given(postService.createPost(eq(nonExistBoardId), anyLong(), any(CreatePostRequest.class)))
                .willThrow(new ApiException(ErrorMessage.NOT_FOUND, PostErrorCode.BOARD_NOT_FOUND));

        // == When ==
        ResultActions performResult = mockMvc.perform(post("/boards/{boardId}/posts", nonExistBoardId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(mockUser))
                .content(objectMapper.writeValueAsString(mockRequest)));

        // == Then ==
        performResult
                .andExpect(status().isNotFound()) // 1. 驗證 HTTP 404
                .andExpect(jsonPath("$.code").value(PostErrorCode.BOARD_NOT_FOUND.name())) // 2. 驗證錯誤代碼
                .andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND.name()));   // 3. 驗證錯誤訊息
    }
}
