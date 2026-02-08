package com.example.demo.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "BODY_INVALID")
    @Size(min = 1, max = 200, message = "BODY_INVALID")
    @Pattern(regexp = "^[^<>]+$", message = "BODY_INVALID") // 禁止 < 和 >
    private String body;
}
