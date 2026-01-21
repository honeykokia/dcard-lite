package com.example.demo.board.controller;

import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.service.BoardService;
import com.example.demo.post.dto.CreatePostRequest;
import com.example.demo.post.dto.CreatePostResponse;
import com.example.demo.post.service.PostService;
import com.example.demo.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<ListBoardsResponse> listBoards(@Valid @ModelAttribute ListBoardsRequest request) {

        ListBoardsResponse response = boardService.listBoards(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{boardId}/posts")
    public ResponseEntity<CreatePostResponse> createPost(
            @PathVariable @Positive(message = "PATH_FORMAT_ERROR") Long boardId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePostRequest request
    ) {
        long userId = user.getUserId();
        CreatePostResponse response = postService.createPost(boardId,userId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
