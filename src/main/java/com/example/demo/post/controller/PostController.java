package com.example.demo.post.controller;

import com.example.demo.board.dto.GetPostResponse;
import com.example.demo.post.service.PostService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<GetPostResponse> getPost(@Positive(message = "PATH_FORMAT_ERROR") @PathVariable Integer postId) {
        GetPostResponse response = postService.getPost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
