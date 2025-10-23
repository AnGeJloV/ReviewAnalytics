package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.model.ErrorResponseDto;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

/**
 * Сервис для взаимодействия с REST API категорий на сервере.
 */

public class CategoryService {
    private static final String BASE_URL = "http://localhost:8080/api/categories";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Получает все категории с сервера.
     */
    public List<CategoryDto> getAllCategories() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // Используем TypeReference для парсинга списка объектов
            return objectMapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public CategoryDto createCategory(CategoryDto category) throws IOException {
        String json = objectMapper.writeValueAsString(category);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(BASE_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            return processResponse(response, CategoryDto.class);
        }
    }

    public CategoryDto updateCategory(CategoryDto category) throws IOException {
        String json = objectMapper.writeValueAsString(category);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + category.getId())
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), CategoryDto.class);
        }
    }

    public void deleteCategory(Long id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    /**
     * Вспомогательный метод для обработки ответа от сервера.
     * Либо возвращает распарсенный объект, либо выбрасывает ApiException.
     */
    private <T> T processResponse(Response response, Class<T> clazz) throws IOException {
        if (response.isSuccessful()) {
            return objectMapper.readValue(response.body().string(), clazz);
        } else {
            handleError(response);
            return null; // Сюда выполнение не дойдет, так как handleError выбросит исключение
        }
    }

    /**
     * Вспомогательный метод для обработки ошибочного ответа.
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
