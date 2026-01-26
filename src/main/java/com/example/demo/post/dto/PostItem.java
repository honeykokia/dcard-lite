package com.example.demo.post.dto;

import com.example.demo.post.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostItem {
    private long postId;
    private long authorId;
    private String authorName;
    private long boardId;
    private String boardName;
    private String title;
    private int likeCount;
    private Double hotScore;
    private PostStatus status;
    private Instant createdAt;
}
