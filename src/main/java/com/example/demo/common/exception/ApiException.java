package com.example.demo.common.exception;

import com.example.demo.common.error.ErrorCode;
import com.example.demo.common.error.ErrorMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{
    private final HttpStatus status;
    private final ErrorMessage errorMessage;
    private final ErrorCode errorCode;

    public ApiException(ErrorMessage errorMessage, ErrorCode errorCode){
    super(errorMessage.name());
        this.status = errorMessage.getStatus();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
}
