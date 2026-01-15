package com.example.demo.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "EMAIL_INVALID")
    private String email;
    @NotBlank(message = "PASSWORD_INVALID")
    private String password;
}
