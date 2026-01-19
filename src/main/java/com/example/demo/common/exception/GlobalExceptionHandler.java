package com.example.demo.common.exception;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;



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
        String errorCode = ex.getBindingResult().getFieldErrors().stream()
                .findFirst() // 1. 安全地嘗試抓第一個，抓不到就是 Optional.empty
                .map(error -> {
                    // 2. 這裡放入你原本的邏輯
                    if (error.isBindingFailure()) {
                        return "PARAM_FORMAT_ERROR"; // 如果是型別轉換失敗 (如 String 轉 int)
                    }
                    return error.getDefaultMessage(); // 如果是驗證失敗 (@NotNull, @Size)
                })
                .orElse("UNKNOWN_ERROR"); // 3. 如果真的完全沒錯誤 (List為空) 的預設值

        // 2. 組裝你的固定 Response 格式
        ErrorResponse response = new ErrorResponse(
                400,
                "Bad Request",
                ErrorMessage.VALIDATION_FAILED, // 固定大類
                errorCode,          // 動態細項
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
