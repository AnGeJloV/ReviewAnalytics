package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

/**
 * DTO для ответа сервера после успешной аутентификации.
 * Содержит JWT-токен, который клиент будет использовать для последующих запросов.
 */

@Data
public class AuthResponse {
    private String token;
}
