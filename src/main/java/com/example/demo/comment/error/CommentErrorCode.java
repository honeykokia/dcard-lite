package com.example.demo.comment.error;

import com.example.demo.common.error.ErrorCode;

public enum CommentErrorCode implements ErrorCode {
    PATH_FORMAT_ERROR,
    BODY_INVALID,
    SECURITY_UNAUTHORIZED,
    POST_NOT_FOUND,
    ;


    @Override
    public String code() {
        return name();
    }
}
