package com.github.stasangelov.reviewanalytics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Кастомное исключение, выбрасываемое, когда операция запрещена бизнес-логикой.
 * Например, при попытке пользователя изменить собственную роль или заблокировать
 * свою учетную запись.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(String message) {
        super(message);
    }
}
