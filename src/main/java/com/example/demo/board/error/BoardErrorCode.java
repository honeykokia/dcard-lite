package com.example.demo.board.error;


import com.example.demo.common.error.ErrorCode;

public enum BoardErrorCode implements ErrorCode {
    PAGE_INVALID,
    PAGE_SIZE_INVALID,
    KEYWORD_INVALID;


    public String code() {
        return name();
    }
}
