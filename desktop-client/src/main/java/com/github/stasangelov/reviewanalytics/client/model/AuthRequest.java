package com.github.stasangelov.reviewanalytics.client.model;

import lombok.Data;

/**
 * DTO для запроса на аутентификацию (вход в систему).
 */

@Data
public class AuthRequest {
    private String email;
    private String password;
}
