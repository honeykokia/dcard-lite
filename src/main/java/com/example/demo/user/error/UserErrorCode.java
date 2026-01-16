package com.example.demo.user.error;


import com.example.demo.common.error.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    NAME_INVALID,
    EMAIL_INVALID,
    PASSWORD_INVALID,
    CONFIRM_PASSWORD_INVALID,
    AUTHENTICATION_FAILED,
    EMAIL_ALREADY_EXISTS;

    @Override
    public String code() {
        return name();
    }
}
