package com.example.demo.post.dto;

import com.example.demo.post.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeletePostResponse {
    private long postId;
    private PostStatus postStatus;
}
