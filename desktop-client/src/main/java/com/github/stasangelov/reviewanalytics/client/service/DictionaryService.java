package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.dictionary.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.model.dictionary.CriterionDto;
import com.github.stasangelov.reviewanalytics.client.model.dictionary.ProductDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;

/**
 * Сервис для загрузки "справочных" данных с сервера.
 * Инкапсулирует все запросы к API для получения списков категорий,
 * критериев и товаров, которые редко меняются и могут быть кэшированы
 * или загружены один раз при запуске определенных окон.
 */
public class DictionaryService {

    // --- Константы и поля ---
    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //================================================================================
    // Публичные методы API
    //================================================================================

    /**
     * Загружает с сервера полный список всех категорий товаров.
     */
    public List<CategoryDto> getAllCategories() throws IOException {
        return fetchList(BASE_URL + "/categories", new TypeReference<>() {});
    }

    /**
     * Загружает с сервера полный список всех критериев оценки.
     */
    public List<CriterionDto> getAllCriteria() throws IOException {
        return fetchList(BASE_URL + "/criteria", new TypeReference<>() {});
    }

    /**
     * Загружает с сервера полный список всех товаров.
     */
    public List<ProductDto> getAllProducts() throws IOException {
        return fetchList(BASE_URL + "/products", new TypeReference<>() {});
    }

    /**
     * Загружает с сервера список критериев, применимых к конкретной категории.
     */
    public List<CriterionDto> getCriteriaByCategoryId(Long categoryId) throws IOException {
        String url = BASE_URL + "/criteria/by-category/" + categoryId;
        return fetchList(url, new TypeReference<>() {});
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Универсальный приватный метод для выполнения GET-запроса и парсинга ответа в список объектов.
     * Используется всеми публичными методами этого класса для избежания дублирования кода.
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