package com.github.stasangelov.reviewanalytics.dto.user;

import lombok.Data;

/**
 * DTO для представления "безопасной" информации о пользователе.
 * Используется в ответах API, чтобы не раскрывать хэш пароля и другую служебную информацию.
 */
@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}