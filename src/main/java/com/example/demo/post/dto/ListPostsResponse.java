package com.example.demo.post.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListPostsResponse {
    private int page;
    private int pageSize;
    private long total;
    private List<PostItem> items;
}
