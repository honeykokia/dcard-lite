package com.example.demo.common.exception;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex , HttpServletRequest request
    ) {
        HttpStatus status = ex.getStatus();
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getErrorMessage(),
                ex.getErrorCode().code(),
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // 1. 取得 @NotNull(message="NAME_INVALID") 裡面的字串
        FieldError error = ex.getBindingResult().getFieldErrors().get(0);
        String errorCode;

        if (error.isBindingFailure()){
            errorCode = "PARAM_FORMAT_ERROR";
        }else{
            errorCode = error.getDefaultMessage();
        }

        // 2. 組裝你的固定 Response 格式
        ErrorResponse reponse = new ErrorResponse(
                400,
                "Bad Request",
                ErrorMessage.VALIDATION_FAILED, // 固定大類
                errorCode,          // 動態細項
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }

}
