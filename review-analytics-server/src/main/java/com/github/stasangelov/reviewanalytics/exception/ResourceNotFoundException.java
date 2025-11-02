package com.github.stasangelov.reviewanalytics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Кастомное исключение, выбрасываемое, когда запрашиваемый ресурс
 * (например, категория, товар) не может быть найден в базе данных.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
