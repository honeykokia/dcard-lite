package com.example.demo.common.util;

import com.example.demo.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtServiceTest {
    @Autowired
    private JwtService jwtService;

    private UserDetails mockUser;

    @BeforeEach
    void setUp() {
        // 模擬一個簡單的 Spring Security User
        mockUser = new User("leo@example.com", "password", new ArrayList<>());
    }

    @Test
    void testGenerateAndValidateToken() {
        // 1. 產生 Token
        String token = jwtService.generateToken(mockUser);

        System.out.println("Generated Token: " + token); // 印出來看看長什麼樣

        // 2. 驗證 Token 格式
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 3. 驗證內容 (Extract Username)
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("leo@example.com", extractedUsername);

        // 4. 驗證有效性
        assertTrue(jwtService.isTokenValid(token, mockUser));
    }
}
