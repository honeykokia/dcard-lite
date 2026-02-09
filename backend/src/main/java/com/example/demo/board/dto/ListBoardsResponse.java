package com.example.demo.board.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListBoardsResponse {

    private int page;
    private int pageSize;
    private long total;
    private List<BoardItem> items;
}
