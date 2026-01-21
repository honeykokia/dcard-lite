package com.example.demo.user.controller;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.common.exception.GlobalExceptionHandler;
import com.example.demo.common.security.JwtAuthenticationEntryPoint;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.SecurityConfig;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RegisterUserRequest;
import com.example.demo.user.dto.RegisterUserResponse;
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.error.UserErrorCode;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
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
    void registerUser_NameInvalid_Return400(String invalidName) throws Exception {
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
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.NAME_INVALID.name()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "invalid_email",
            "email_is_too_longjfdhagklahsdlgjhlsdahgkjlasdhglkhasdklghlkasdhgklhsdgklhsldkaghlkahdsgklhsadiughrialwhgiurengilnsiulgnisdgn"
    })
    @NullSource
    void registerUser_EmailInvalid_Return400(String invalidEmail) throws Exception {
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
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.EMAIL_INVALID.name()));

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
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.PASSWORD_INVALID.name()));
    }

    @Test
    void registerUser_PasswordsDoNotMatch_Return400() throws Exception {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        given(userService.registerUser(any(RegisterUserRequest.class)))
                .willThrow(new ApiException(ErrorMessage.VALIDATION_FAILED,
                        UserErrorCode.CONFIRM_PASSWORD_INVALID));

        // == When ==
        ResultActions result = mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // == Then ==
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.CONFIRM_PASSWORD_INVALID.name()));

    }

    @Test
    void registerUser_EmailAlreadyExists_Return409() throws Exception {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        given(userService.registerUser(any(RegisterUserRequest.class)))
                .willThrow(new ApiException(ErrorMessage.CONFLICT,
                        UserErrorCode.EMAIL_ALREADY_EXISTS));
        // == When ==
        ResultActions result = mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // == Then ==
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.EMAIL_ALREADY_EXISTS.name()));
    }

    @Test
    void loginUser_Success() throws Exception {
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
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.EMAIL_INVALID.name()));
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
                .andExpect(jsonPath("$.message").value(ErrorMessage.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.PASSWORD_INVALID.name()));
    }

    @Test
    void loginUser_IncorrectPassword_Return401() throws Exception {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("wrong_password");

        given(userService.loginUser(any(LoginRequest.class))).willThrow(new ApiException(ErrorMessage.UNAUTHORIZED, UserErrorCode.AUTHENTICATION_FAILED));

        // == When ==
        ResultActions result = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // == Then ==
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.AUTHENTICATION_FAILED.name()));

    }

    @Test
    void loginUser_EmailDoesNotExist_Return401() throws Exception {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("inavlidEmail");
        request.setPassword("abc12345");
        given(userService.loginUser(any(LoginRequest.class))).willThrow(new ApiException(ErrorMessage.UNAUTHORIZED, UserErrorCode.AUTHENTICATION_FAILED));

        // == When ==
        ResultActions result = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // == Then ==
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.code").value(UserErrorCode.AUTHENTICATION_FAILED.name()));

    }
}