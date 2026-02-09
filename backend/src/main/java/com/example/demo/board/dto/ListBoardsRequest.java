package com.example.demo.board.dto;

import com.example.demo.board.error.BoardErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ListBoardsRequest {

    @Min(value = 1, message = "PAGE_INVALID")
    private Integer page = 1;

    @Min(value = 1, message = "PAGE_SIZE_INVALID")
    @Max(value = 100, message = "PAGE_SIZE_INVALID")
    private Integer pageSize = 20;

    @Size(max = 50, message = "KEYWORD_INVALID")
    private String keyword;

    /**
     * 覆寫 Lombok 的 Setter，實作 Business Logic：
     * 1. Trim 空白
     * 2. 空字串視為 null (未提供)
     */
    public void setKeyword(String keyword) {
        if (keyword != null) {
            String trimmed = keyword.trim();
            // 邏輯：空白字串視為 null，否則保留 trim 後的值
            this.keyword = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.keyword = null;
        }
    }
}
