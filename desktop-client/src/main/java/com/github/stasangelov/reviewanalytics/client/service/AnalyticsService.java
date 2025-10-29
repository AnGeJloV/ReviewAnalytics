package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.DashboardDto;
import com.github.stasangelov.reviewanalytics.client.model.ErrorResponseDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Сервис для получения агрегированных аналитических данных с сервера.
 */
public class AnalyticsService {
    private static final String BASE_URL = "http://localhost:8080/api/analytics";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Запрашивает и возвращает все данные для главного дашборда.
     * @return DashboardDto, содержащий всю необходимую аналитику.
     * @throws IOException если произошла ошибка сети или API.
     */
    public DashboardDto getDashboardData() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/dashboard")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), DashboardDto.class);
            } else {
                handleError(response);
                return null; // Сюда выполнение не дойдет
            }
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