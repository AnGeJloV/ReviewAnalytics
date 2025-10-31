package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.stasangelov.reviewanalytics.client.model.common.ErrorResponseDto;
import com.github.stasangelov.reviewanalytics.client.model.review.ReviewDto;
import okhttp3.*;
import java.io.IOException;
import java.util.List;

/**
 * Сервис для взаимодействия с API управления отзывами (`/api/reviews`).
 * Предоставляет полный набор CRUD-операций (Create, Read, Update, Delete)
 * для работы с отзывами.
 */
public class ReviewService {

    // --- Константы и поля ---
    private static final String BASE_URL = "http://localhost:8080/api/reviews";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Конструктор. Регистрирует модуль для корректной работы с типами Java 8 Date/Time.
     */
    public ReviewService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    //================================================================================
    // Публичные методы API
    //================================================================================

    /**
     * Загружает с сервера полный список всех отзывов.
     */
    public List<ReviewDto> getAllReviews() throws IOException {
        Request request = new Request.Builder().url(BASE_URL).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) handleError(response);
            return objectMapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    /**
     * Отправляет на сервер запрос на создание нового отзыва.
     */
    public ReviewDto createReview(ReviewDto review) throws IOException {
        String json = objectMapper.writeValueAsString(review);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(BASE_URL).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response, ReviewDto.class);
        }
    }

    /**
     * Отправляет на сервер запрос на полное обновление существующего отзыва.
     */
    public ReviewDto updateReview(Long id, ReviewDto review) throws IOException {
        String json = objectMapper.writeValueAsString(review);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(BASE_URL + "/" + id).put(body).build();
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response, ReviewDto.class);
        }
    }

    /**
     * Отправляет на сервер запрос на изменение статуса отзыва (например, "ACTIVE" или "REJECTED").
     */
    public void changeStatus(Long id, String status) throws IOException {
        String json = "{\"status\":\"" + status + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(BASE_URL + "/" + id + "/status").patch(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) handleError(response);
        }
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Универсальный обработчик успешных ответов.
     * Парсит тело ответа в указанный класс DTO.
     */
    private <T> T processResponse(Response response, Class<T> clazz) throws IOException {
        if (response.isSuccessful()) {
            return objectMapper.readValue(response.body().string(), clazz);
        }
        handleError(response);
        return null;
    }

    /**
     * Обрабатывает ошибки ответа сервера.
     */
    private void handleError(Response response) throws IOException {
        String errorBody = response.body().string();
        try {
            ErrorResponseDto errorDto = objectMapper.readValue(errorBody, ErrorResponseDto.class);
            throw new ApiException(response.code(), errorDto.getMessage());
        } catch (Exception e) {
            throw new ApiException(response.code(), "Не удалось распознать ошибку: " + errorBody);
        }
    }
}