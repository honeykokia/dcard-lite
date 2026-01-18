package com.example.demo.board.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardItem {
    private Long boardId;
    private String name;
    private String description;
}
