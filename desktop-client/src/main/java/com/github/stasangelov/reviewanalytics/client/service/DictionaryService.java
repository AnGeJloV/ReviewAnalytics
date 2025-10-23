package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.model.CriterionDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;

/**
 * Сервис для загрузки "справочных" данных с сервера (товары, категории, критерии).
 */
public class DictionaryService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CategoryDto> getAllCategories() throws IOException {
        return fetchList(BASE_URL + "/categories", new TypeReference<>() {});
    }

    public List<CriterionDto> getAllCriteria() throws IOException {
        return fetchList(BASE_URL + "/criteria", new TypeReference<>() {});
    }

    public List<ProductDto> getAllProducts() throws IOException {
        return fetchList(BASE_URL + "/products", new TypeReference<>() {});
    }

    /**
     * Универсальный метод для выполнения GET-запроса и парсинга списка объектов.
     */
    private <T> List<T> fetchList(String url, TypeReference<List<T>> typeReference) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // В будущем можно будет улучшить обработку ошибок
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), typeReference);
        }
    }
}