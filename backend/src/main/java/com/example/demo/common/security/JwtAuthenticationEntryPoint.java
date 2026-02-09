package com.example.demo.common.security;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 1. 設定 HTTP 狀態碼為 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 2. 設定回傳格式為 JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 3. 準備錯誤訊息
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.setError("Unauthorized");
        errorResponse.setMessage(ErrorMessage.UNAUTHORIZED);
        errorResponse.setCode("SECURITY_UNAUTHORIZED");
        errorResponse.setPath(request.getRequestURI()); // 抓取當前請求的路徑
        errorResponse.setTimestamp(Instant.now());

        // 4. 手動將 Map 轉成 JSON 字串寫出去
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}