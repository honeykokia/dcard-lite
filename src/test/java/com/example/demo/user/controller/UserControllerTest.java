package com.example.demo.user.controller;

import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RegisterUserRequest;
import com.example.demo.user.dto.RegisterUserResponse;
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    void registerUser_Success() throws Exception {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        RegisterUserResponse response = new RegisterUserResponse();
        response.setUserId(1L);
        response.setDisplayName("Leo");
        response.setEmail("leo@example.com");
        response.setRole(UserRole.USER);
        response.setCreatedAt(Instant.now());

        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(response);

        // == When & Then ==
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("leo@example.com"));

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "ThisNameIsWayTooLongToBeValidBecauseItExceedsTheMaximumAllowedLengthOfFiftyCharacters",
            "123456",
            "!@#$%"
    })
    @NullSource
    void registerUser_NameInvalid_Return400(String invalidName) throws Exception{
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName(invalidName); // Invalid name
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        // == When & Then ==
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("NAME_INVALID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "invalid_email",
            "email_is_too_longjfdhagklahsdlgjhlsdahgkjlasdhglkhasdklghlkasdhgklhsdgklhsldkaghlkahdsgklhsadiughrialwhgiurengilnsiulgnisdgn"
    })
    @NullSource
    void registerUser_EmailInvalid_Return400(String invalidEmail) throws Exception{
        // == Giver ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail(invalidEmail); // Invalid email
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        // == When & Then ==
        mockMvc.perform(post("/users/register")
        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("EMAIL_INVALID"));

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "short",
            "thisPasswordIsWayTooLongToBeConsideredValid123",
            "allEnglishletters",
            "allDigits"
    })
    void registerUser_PasswordInvalid_Return400(String invalidPassword) throws Exception {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword(invalidPassword); // Invalid password
        request.setConfirmPassword("abc12345");
        // == When & Then ==
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("PASSWORD_INVALID"));
    }

    @Test
    void loginUser_Success() throws Exception{
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");

        LoginResponse response = new LoginResponse();
        response.setUserId(1L);
        response.setDisplayName("Leo");
        response.setRole(UserRole.USER);
        response.setAccessToken("jwt_token");

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);
        // == When & Then ==
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accessToken").value("jwt_token"));

    }

    @Test
    void loginUser_EmailInvalid_Return400() throws Exception {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("abc12345");

        // == When & Then ==
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("EMAIL_INVALID"));
    }

    @Test
    void loginUser_PasswordInvalid_Return400() throws Exception {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("");
        // == When & Then ==
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("PASSWORD_INVALID"));
    }
}
