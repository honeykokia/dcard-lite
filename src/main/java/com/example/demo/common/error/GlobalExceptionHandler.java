package com.example.demo.common.error;

import com.example.demo.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                ex.getMessage(),
                ex.getCode().code(),
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }
}
