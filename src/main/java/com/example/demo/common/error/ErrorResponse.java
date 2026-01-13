package com.example.demo.common.error;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String code;
    private String path;
    private Instant timestamp;
}
