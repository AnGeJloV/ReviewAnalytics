package com.github.stasangelov.reviewanalytics.exception;


import com.github.stasangelov.reviewanalytics.dto.common.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Аннотация @ControllerAdvice позволяет этому классу перехватывать исключения,
 * выброшенные любым контроллером, и формировать стандартизированный ответ клиенту.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Перехватывает исключение при неудачной попытке входа (неверный email или пароль).
     * Возвращает статус 403 Forbidden.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Перехватывает ошибки валидации DTO (например, в {@code @Valid RegistrationRequest}).
     * Собирает все сообщения об ошибках полей в одну строку.
     * Возвращает статус 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Перехватывает исключение, когда запрашиваемый ресурс не найден в базе данных.
     * Возвращает статус 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Перехватывает исключение для операций, запрещенных бизнес-логикой
     * (например, попытка администратора заблокировать самого себя).
     * Возвращает статус 403 Forbidden.
     */
    @ExceptionHandler(OperationForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleOperationForbidden(OperationForbiddenException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
