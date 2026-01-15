package com.example.demo.user.dto;

import com.example.demo.user.entity.UserRole;
import lombok.Data;

import java.time.Instant;

@Data
public class RegisterUserResponse {
    private long userId;
    private String displayName;
    private String email;
    private UserRole role;
    private Instant createdAt;
}


