package com.example.demo.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Lombok 會幫忙生成 final 欄位的建構子 (DI)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // 這是 Spring Security 內建的介面，等下我們要去實作它

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 從 Header 取得 Authorization 欄位
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. 檢查 Header 格式：必須以 "Bearer " 開頭
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 如果沒有 Token，就直接放行給後面的過濾器處理 (SecurityConfig 會決定要不要擋)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 Token (去掉 "Bearer " 這 7 個字)
        jwt = authHeader.substring(7);

        // 4. 從 Token 提取 Email (Username)
        userEmail = jwtService.extractUsername(jwt);

        // 5. 關鍵邏輯：
        // (A) Email 必須存在
        // (B) SecurityContextHolder 目前必須是空的 (代表這個人還沒被認證過)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 去資料庫查這個人的完整資料 (包含密碼、權限)
            // 注意：這裡可能會紅字，因為你可能還沒實作 UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. 驗證 Token 是否有效
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. 製作身分證 (UsernamePasswordAuthenticationToken)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 這裡通常放 null 或 credentials，因為是 JWT 登入所以不需要密碼
                        userDetails.getAuthorities()
                );

                // 把請求的細節 (IP, Session ID 等) 補進去
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. 【最終目標】將身分證放入 SecurityContext，代表 "登入成功"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. 任務完成，交棒給下一個 Filter
        filterChain.doFilter(request, response);
    }
}
