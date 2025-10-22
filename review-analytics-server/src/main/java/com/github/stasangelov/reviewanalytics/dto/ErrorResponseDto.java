package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponseDto {
    private String timestamp;
    private String message;
    private int status;

    public ErrorResponseDto (int status, String message) {
        this.timestamp = LocalDateTime.now().toString();
        this.message = message;
        this.status = status;
    }
}
