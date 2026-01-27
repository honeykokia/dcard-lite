package com.example.demo.post.error;

import com.example.demo.common.error.ErrorCode;

public enum PostErrorCode implements ErrorCode {
    BOARD_NOT_FOUND,
    USER_NOT_FOUND,
    SECURITY_UNAUTHORIZED,
    PATH_FORMAT_ERROR,
    BODY_INVALID,
    TITLE_INVALID,
    PAGE_INVALID,
    PAGE_SIZE_INVALID,
    SORT_INVALID;



    @Override
    public String code() {
        return name();
    }
}
