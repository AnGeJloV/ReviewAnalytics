package com.github.stasangelov.reviewanalytics.dto.auth;

import lombok.Data;

import java.util.Set;

/**
 * DTO для ответа сервера после успешной аутентификации.
 * Содержит JWT-токен, который клиент будет использовать для последующих запросов.
 */
@Data
public class AuthResponse {
    private String token;
    private Set<String> roles;
}
