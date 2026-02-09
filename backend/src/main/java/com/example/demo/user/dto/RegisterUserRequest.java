package com.example.demo.user.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    // 不可純數字、不可純符號（僅非英數），且至少 1 字元
    private static final String NAME_REGEX = "^(?!\\d+$)(?!\\P{Alnum}+$).+$";

    // 8~12、至少一個字母、一個數字、不可含空白（與 rp-001 規則一致）
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?!.*\\s).{8,12}$";

    @NotBlank(message = "NAME_INVALID")
    @Size(max = 20, message = "NAME_INVALID")
    @Pattern(regexp = NAME_REGEX, message = "NAME_INVALID")
    private String name;

    @NotBlank(message = "EMAIL_INVALID")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 100, message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = 8, max = 12, message = "PASSWORD_INVALID")
    @Pattern(regexp = PASSWORD_REGEX, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "CONFIRM_PASSWORD_INVALID")
    @Size(min = 8, max = 12, message = "CONFIRM_PASSWORD_INVALID")
    private String confirmPassword;

}
