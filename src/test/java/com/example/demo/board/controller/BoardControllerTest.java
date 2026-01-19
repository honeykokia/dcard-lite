package com.example.demo.board.controller;

import com.example.demo.board.dto.BoardItem;
import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.service.BoardService;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BoardController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
    void listBoards_PageInvalid_Return400(String page, String exceptedErrorCode) throws Exception{
        // == When & Then ==
        mockMvc.perform(get("/boards")
                .param("page",page) // Invalid page
                .param("pageSize","20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value(exceptedErrorCode));
    }


    @ParameterizedTest(name = "測試案例: pageSize={0}, 預期錯誤={1}")
    @CsvSource({
            "0,      PAGE_SIZE_INVALID",
            "101,     PAGE_SIZE_INVALID",
            "invalid,               PARAM_FORMAT_ERROR"
    })
    void listBoards_PageSizeInvalid_Return400(String pageSize, String exceptedErrorCode) throws Exception{
        // == When & Then ==
        mockMvc.perform(get("/boards")
                        .param("page","1") // Valid page (fixed), testing pageSize validation
                        .param("pageSize",pageSize)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value(exceptedErrorCode));
    }


    static Stream<String> provideInvalidKeywords() {
        return Stream.of(
                // 情境 1: 純粹超過 50 字 (51個a)
                "a".repeat(51)

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
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("KEYWORD_INVALID"));
    }
}
