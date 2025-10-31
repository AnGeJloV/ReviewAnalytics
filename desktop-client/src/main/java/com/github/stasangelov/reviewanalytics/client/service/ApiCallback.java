package com.github.stasangelov.reviewanalytics.client.service;

/**
 * Универсальный интерфейс обратного вызова (callback) для обработки асинхронных сетевых запросов.
 * Он определяет стандартный контракт для обработки трех возможных исходов:
 * - {@code onSuccess}: Успешное получение и обработка данных.
 * - {@code onFailure}: Ошибка на уровне сети (например, нет соединения).
 * - {@code onError}: Ошибка на стороне сервера (например, неверные данные, код 4xx или 5xx).
 * @param <T> Тип объекта, который ожидается получить при успешном ответе.
 */
public interface ApiCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
    void onError(int code, String message);
}