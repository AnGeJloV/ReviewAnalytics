package com.github.stasangelov.reviewanalytics.client.service;

import lombok.Getter;

import java.io.IOException;

/**
 * Кастомное исключение для обработки ошибок, полученных от API сервера.
 */

@Getter
public class ApiException extends IOException {
    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
