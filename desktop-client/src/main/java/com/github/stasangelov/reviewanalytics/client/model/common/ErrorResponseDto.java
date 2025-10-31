package com.github.stasangelov.reviewanalytics.client.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для стандартизированного ответа об ошибке от сервера.
 * Позволяет клиенту удобно парсить и отображать сообщения об ошибках.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponseDto {
    private String timestamp;
    private String message;
    private int status;
}
