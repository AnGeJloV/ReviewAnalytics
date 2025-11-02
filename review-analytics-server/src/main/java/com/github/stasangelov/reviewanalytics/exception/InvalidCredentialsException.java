package com.github.stasangelov.reviewanalytics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Кастомное исключение, которое создается и выбрасывается в одном
 * конкретном случае: когда пользователь пытается войти в систему, но предоставляет
 * неверные учетные данные (email или пароль).
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
