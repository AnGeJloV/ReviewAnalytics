package com.github.stasangelov.reviewanalytics.client.service;

import lombok.Getter;
import java.io.IOException;

/**
 * Кастомное (пользовательское) исключение для обработки ошибок, полученных от API сервера.
 * Оно наследуется от IOException, чтобы быть совместимым с сетевыми методами,
 * но при этом несет дополнительную информацию: HTTP-статус код.
 */
@Getter
public class ApiException extends IOException {
    private final int statusCode;

    /**
     * Конструктор для создания экземпляра исключения.
     * @param statusCode HTTP-статус код.
     * @param message Понятное сообщение об ошибке, извлеченное из тела ответа сервера.
     */
    public ApiException(int statusCode, String message) {
        super(message); // Передаем сообщение в родительский класс IOException
        this.statusCode = statusCode;
    }
}