package com.example.demo.user.service;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RegisterUserRequest;
import com.example.demo.user.dto.RegisterUserResponse;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.error.UserErrorCode;
import com.example.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterUserResponse registerUser(RegisterUserRequest registerUserRequest) {

        String password = registerUserRequest.getPassword();
        String confirmPassword = registerUserRequest.getConfirmPassword();
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            throw new ApiException(ErrorMessage.VALIDATION_FAILED, UserErrorCode.CONFIRM_PASSWORD_INVALID);
        }

        String email = registerUserRequest.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorMessage.CONFLICT, UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setDisplayName(registerUserRequest.getName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setCreatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        RegisterUserResponse response = new RegisterUserResponse();
        response.setUserId(savedUser.getUserId());
        response.setDisplayName(savedUser.getDisplayName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setCreatedAt(savedUser.getCreatedAt());
        return response;
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorMessage.UNAUTHORIZED, UserErrorCode.AUTHENTICATION_FAILED));

        boolean ok = passwordEncoder.matches(password, user.getPasswordHash());
        if (!ok) {
            throw new ApiException(ErrorMessage.UNAUTHORIZED, UserErrorCode.AUTHENTICATION_FAILED);
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setDisplayName(user.getDisplayName());
        response.setRole(user.getRole());
        // 測試只要求非 null；先給一個簡單 token（之後可換成 JWT）
        response.setAccessToken(UUID.randomUUID().toString());
        return response;
    }

}
