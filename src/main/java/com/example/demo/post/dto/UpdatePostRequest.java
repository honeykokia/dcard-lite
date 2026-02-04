package com.example.demo.post.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdatePostRequest {

    @Length(min = 1, max = 50, message = "TITLE_INVALID")
    @Pattern(regexp = "^(?=.*[^\\s])[^<>]+$", message = "TITLE_INVALID")
    private String title;

    @Length(min = 1, max = 300, message = "BODY_INVALID")
    @Pattern(regexp = "^(?s)(?=.*[^\\s])[^<>]+$", message = "BODY_INVALID")
    private String body;
}
