package com.example.demo.post.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {

    @Size(min = 1, max = 50, message = "TITLE_INVALID")
    @Pattern(regexp = "^(?=.*[^\\s])[^<>]+$", message = "TITLE_INVALID")
    private String title;

    @Size(min = 1, max = 300, message = "BODY_INVALID")
    @Pattern(regexp = "^(?s)(?=.*[^\\s])[^<>]+$", message = "BODY_INVALID")
    private String body;
}
