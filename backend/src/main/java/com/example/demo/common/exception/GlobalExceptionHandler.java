package com.example.demo.common.exception;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // 1. 處理 DTO 驗證失敗 (@Valid, @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleDtoValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorCode = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> {
                    if (error.isBindingFailure()) return "PARAM_FORMAT_ERROR"; // JSON 欄位型別錯
                    return error.getDefaultMessage(); // DTO 上的 message
                })
                .orElse("VALIDATION_FAILED");

        return buildErrorResponse(errorCode, request);
    }

    // 2. 處理路徑參數驗證失敗 (@Validated, @PathVariable, e.g. "-1")
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handlePathValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        String errorCode = ex.getParameterValidationResults().stream()
                // 1. 拿出所有參數的驗證結果
                .flatMap(result -> result.getResolvableErrors().stream())
                // 2. 找到第一個錯誤
                .findFirst()
                // 3. 取得你在 @Positive(message="PATH_FORMAT_ERROR") 裡寫的訊息
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse("VALIDATION_FAILED");

        return buildErrorResponse(errorCode, request);
    }

    // 3. 處理路徑參數型別錯誤 (e.g. "abc" 轉不成 Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return buildErrorResponse("PATH_FORMAT_ERROR", request);
    }

    // 4. 如果加上@Validate 處理「違反驗證規則」的錯誤會進到這 (例如：@Min, @NotBlank 在 PathVariable 上失效)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        return buildErrorResponse("PATH_FORMAT_ERROR", request);
    }

    // ==========================================
    // 👇 私有共用方法：統一負責 "組裝" 回傳格式
    // ==========================================
    private ResponseEntity<ErrorResponse> buildErrorResponse(String errorCode, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                400,
                "Bad Request",
                ErrorMessage.VALIDATION_FAILED, // 大類
                errorCode,          // 細項 (從上面傳進來的)
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
