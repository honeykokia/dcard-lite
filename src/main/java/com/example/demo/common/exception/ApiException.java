package com.example.demo.common.exception;

import com.example.demo.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{
    private final HttpStatus status;
    private final ErrorCode code;

    public ApiException(HttpStatus status, String messageKey, ErrorCode code){
    super(messageKey);
        this.status = status;
        this.code = code;
    }
}
