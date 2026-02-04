package com.example.demo.post.controller;

import com.example.demo.post.dto.DeletePostResponse;
import com.example.demo.post.dto.GetPostResponse;
import com.example.demo.post.dto.UpdatePostRequest;
import com.example.demo.post.dto.UpdatePostResponse;
import com.example.demo.post.service.PostService;
import com.example.demo.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<GetPostResponse> getPost(@Positive(message = "PATH_FORMAT_ERROR") @PathVariable Long postId) {
        GetPostResponse response = postService.getPost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(
            @PathVariable @Positive(message = "PATH_FORMAT_ERROR") Long postId,
            @AuthenticationPrincipal User user) {
        DeletePostResponse response = postService.deletePost(postId,user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(
            @PathVariable @Positive(message = "PATH_FORMAT_ERROR")  Long postId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePostRequest request) {
        UpdatePostResponse response = postService.updatePost(postId, user, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
