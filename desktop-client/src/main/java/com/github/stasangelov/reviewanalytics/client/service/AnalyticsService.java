package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.stasangelov.reviewanalytics.client.model.DashboardDto;
import com.github.stasangelov.reviewanalytics.client.model.ErrorResponseDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductSummaryDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductDetailsDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис для получения агрегированных аналитических данных с сервера.
 */
public class AnalyticsService {
    private static final String BASE_URL = "http://localhost:8080/api/analytics";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Запрашивает данные для дашборда с учетом фильтров.
     * @param startDate Начальная дата (может быть null).
     * @param endDate Конечная дата (может быть null).
     * @param categoryId ID категории (может быть null).
     * @return DashboardDto.
     */
    public DashboardDto getDashboardData(LocalDate startDate, LocalDate endDate, Long categoryId) throws IOException {
        // Используем HttpUrl.Builder для безопасного построения URL с параметрами
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/dashboard").newBuilder();

        if (startDate != null) {
            urlBuilder.addQueryParameter("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (endDate != null) {
            urlBuilder.addQueryParameter("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (categoryId != null) {
            urlBuilder.addQueryParameter("categoryId", String.valueOf(categoryId));
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), DashboardDto.class);
            } else {
                handleError(response);
                return null;
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
    /**
     * НОВЫЙ МЕТОД: Запрашивает сводную информацию по товарам для таблицы.
     */
    public List<ProductSummaryDto> getProductsSummary(LocalDate startDate, LocalDate endDate, Long categoryId) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/products-summary").newBuilder();

        if (startDate != null) {
            urlBuilder.addQueryParameter("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (endDate != null) {
            urlBuilder.addQueryParameter("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (categoryId != null) {
            urlBuilder.addQueryParameter("categoryId", String.valueOf(categoryId));
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), new TypeReference<>() {});
            } else {
                handleError(response);
                return null;
            }
        }
    }
    /**
     * НОВЫЙ МЕТОД: Запрашивает детализированную информацию по одному товару.
     */
    public ProductDetailsDto getProductDetails(Long productId) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/product/" + productId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), ProductDetailsDto.class);
            } else {
                handleError(response);
                return null;
            }
        }
    }
}