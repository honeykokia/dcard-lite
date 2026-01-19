package com.example.demo.board.service;

import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    void listBoards_Success() {
        // == Given ==
        ListBoardsRequest listBoardsRequest = new ListBoardsRequest();
        listBoardsRequest.setPage(1);
        listBoardsRequest.setPageSize(20);

        Board board1 = new Board(1L,"八卦版","想聊什麼就聊什麼", Instant.now());
        Board board2 = new Board(2L,"軟體版","聊軟體相關的知識", Instant.now());
        Pageable pageable = PageRequest.of(0, 20);
        Page<Board> mockPage = new PageImpl<>(List.of(board1,board2),pageable,2);

        when(boardRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // == When ==
        ListBoardsResponse response = boardService.listBoards(listBoardsRequest);

        // == Then ==
        assertEquals(response.getItems().size(),2);
        assertEquals(response.getPage(),1);
        assertEquals(response.getPageSize(),20);
        assertEquals(response.getTotal(),2);

        verify(boardRepository, times(1)).findAll(any(Pageable.class));
        verify(boardRepository, times(0)).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));

    }

    @Test
    void listBoards_WithKeyword_Success() {
        // == Given ==
        ListBoardsRequest request = new ListBoardsRequest();
        request.setPage(1);
        request.setPageSize(10);
        request.setKeyword("八卦"); // 設定關鍵字

        Board board = new Board(1L,"八卦版","想聊什麼就聊什麼", Instant.now());
        Page<Board> mockPage = new PageImpl<>(List.of(board));

        // 設定 Mock 行為：當呼叫 findByNameContaining 時...
        when(boardRepository.findByNameContainingIgnoreCase(eq("八卦"), any(Pageable.class)))
                .thenReturn(mockPage);

        // == When ==
        ListBoardsResponse response = boardService.listBoards(request);

        // == Then ==
        assertEquals(response.getItems().size(), 1);

        // 關鍵驗證：確認 Service 呼叫了 findByNameContaining
        verify(boardRepository, times(1)).findByNameContainingIgnoreCase(eq("八卦"), any(Pageable.class));
        // 確認沒有呼叫 findAll
        verify(boardRepository, times(0)).findAll(any(Pageable.class));
    }

    @Test
    void listBoards_WithKeyword_ShouldReturnEmptyList() {
        // == Given ==
        ListBoardsRequest request = new ListBoardsRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("其他關鍵字"); // 設定關鍵字

        // 設定 Mock 行為：當呼叫 findByNameContaining 時...
        when(boardRepository.findByNameContainingIgnoreCase(eq("其他關鍵字"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // == When ==
        ListBoardsResponse response = boardService.listBoards(request);

        // == Then ==
        assertEquals(response.getItems().size(), 0);

        // 關鍵驗證：確認 Service 呼叫了 findByNameContaining
        verify(boardRepository, times(1)).findByNameContainingIgnoreCase(eq("其他關鍵字"), any(Pageable.class));
        // 確認沒有呼叫 findAll
        verify(boardRepository, times(0)).findAll(any(Pageable.class));
    }

}
