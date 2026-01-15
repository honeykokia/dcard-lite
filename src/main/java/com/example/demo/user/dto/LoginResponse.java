package com.example.demo.user.dto;

import com.example.demo.user.entity.UserRole;
import lombok.Data;

@Data
public class LoginResponse {
    private long userId;
    private String displayName;
    private UserRole role;
    private String accessToken;
}

