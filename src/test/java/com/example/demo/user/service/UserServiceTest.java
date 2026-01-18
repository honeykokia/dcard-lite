package com.example.demo.user.service;

import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.common.security.JwtService;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RegisterUserRequest;
import com.example.demo.user.dto.RegisterUserResponse;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import com.example.demo.user.error.UserErrorCode;
import com.example.demo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_Success(){
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        //模擬 userRepository.existsByEmail 回傳 false
        when(userRepository.existsByEmail("leo@example.com")).thenReturn(false);
        //模擬 passwordEncoder.encode 回傳 hashed_password
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setDisplayName("Leo");
        savedUser.setEmail("leo@example.com");
        savedUser.setPasswordHash("hashed_password");
        savedUser.setRole(UserRole.USER);
        savedUser.setCreatedAt(Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // == When ==
        RegisterUserResponse response = userService.registerUser(request);

        // == Then ==
        assertNotNull(response);
        assertEquals(1L,response.getUserId());
        assertEquals("Leo",response.getDisplayName());
        assertEquals(UserRole.USER,response.getRole());
        assertEquals("leo@example.com",response.getEmail());
        assertNotNull(response.getCreatedAt());

        verify(passwordEncoder).encode("abc12345");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // == When & Then==
        ApiException exception = assertThrows(ApiException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals(ErrorMessage.CONFLICT,exception.getErrorMessage());
        assertEquals(UserErrorCode.EMAIL_ALREADY_EXISTS,exception.getErrorCode());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void registerUser_PasswordsDoNotMatch_ThrowsException() {
        // == Given ==
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Leo");
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");
        request.setConfirmPassword("differentPassword");
        // == When & Then==
        ApiException exception = assertThrows(ApiException.class, () -> {
            userService.registerUser(request);
        });
        assertEquals(ErrorMessage.VALIDATION_FAILED,exception.getErrorMessage());
        assertEquals(UserErrorCode.CONFIRM_PASSWORD_INVALID,exception.getErrorCode());

        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void loginUser_Success(){
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");

        User finduser = new User();
        finduser.setUserId(1L);
        finduser.setDisplayName("Leo");
        finduser.setEmail("leo@example.com");
        finduser.setPasswordHash("hashed_password");
        finduser.setRole(UserRole.USER);
        finduser.setCreatedAt(Instant.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(finduser));
        when(passwordEncoder.matches(anyString(),anyString())).thenReturn(true);
        when(jwtService.generateToken(anyMap(),any(UserDetails.class))).thenReturn("mocked_jwt_token");

        // == When ==
        LoginResponse response = userService.loginUser(request);

        // == Then ==
        assertNotNull(response);
        assertEquals(1L,response.getUserId());
        assertEquals("Leo",response.getDisplayName());
        assertEquals(UserRole.USER,response.getRole());
        assertEquals(response.getAccessToken(),"mocked_jwt_token");
        assertNotNull(response.getAccessToken());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), finduser.getPasswordHash());

    }

    @Test
    void loginUser_EmailDoesNotExist_ThrowsException() {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("abc12345");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // == When & Then==
        ApiException exception = assertThrows(ApiException.class, () -> {
            userService.loginUser(request);
        });
        assertEquals(ErrorMessage.UNAUTHORIZED,exception.getErrorMessage());
        assertEquals(UserErrorCode.AUTHENTICATION_FAILED,exception.getErrorCode());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder,never()).matches(anyString(),anyString());
    }

    @Test
    void loginUser_IncorrectPassword_ThrowsException() {
        // == Given ==
        LoginRequest request = new LoginRequest();
        request.setEmail("leo@example.com");
        request.setPassword("wrongPassword");

        User finduser = new User();
        finduser.setUserId(1L);
        finduser.setDisplayName("Leo");
        finduser.setEmail("leo@example.com");
        finduser.setPasswordHash("hashed_password");
        finduser.setRole(UserRole.USER);
        finduser.setCreatedAt(Instant.now());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(finduser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // == When & Then==
        ApiException exception = assertThrows(ApiException.class, () -> {
            userService.loginUser(request);
        });
        assertEquals(ErrorMessage.UNAUTHORIZED, exception.getErrorMessage());
        assertEquals(UserErrorCode.AUTHENTICATION_FAILED, exception.getErrorCode());
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), finduser.getPasswordHash());
    }
}
