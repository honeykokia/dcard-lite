package com.example.demo.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetPostResponse {
    private long postId;
    private long authorId;
    private String authorName;
    private long boardId;
    private String boardName;
    private String title;
    private String body;
    private int likeCount;
    private int commentCount;
    private Instant createdAt;

}
