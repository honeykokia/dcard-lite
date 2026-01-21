package com.example.demo.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotBlank(message = "TITLE_INVALID")
    @Size(max = 50, message = "TITLE_INVALID")
    @Pattern(regexp = "^(?!.*[<>]).*$", message = "TITLE_INVALID") // 禁止 < 和 >
    private String title;

    @NotBlank(message = "BODY_INVALID")
    @Size(max = 300, message = "BODY_INVALID")
    @Pattern(regexp = "^(?!.*[<>]).*$", message = "BODY_INVALID") // 禁止 < 和 >
    private String body;
}
