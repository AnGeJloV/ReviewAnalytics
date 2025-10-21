package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * Содержит данные, которые клиент отправляет на сервер для создания учетной записи.
 */

@Data
public class RegistrationRequest {
    private String name;
    private String email;
    private String password;
}
