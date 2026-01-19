package com.example.demo.board.controller;

import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<ListBoardsResponse> listBoards(@Valid @ModelAttribute ListBoardsRequest request) {

        ListBoardsResponse response = boardService.listBoards(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
