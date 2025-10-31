package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.auth.AuthRequest;
import com.github.stasangelov.reviewanalytics.client.model.auth.AuthResponse;
import com.github.stasangelov.reviewanalytics.client.model.auth.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.client.model.common.ErrorResponseDto;
import com.github.stasangelov.reviewanalytics.client.model.user.UserDto;
import okhttp3.*;
import java.io.IOException;

/**
 * Сервис для взаимодействия с API аутентификации на сервере (`/api/auth`).
 * Отвечает за выполнение асинхронных запросов на регистрацию и вход в систему.
 * Результаты операций передаются через интерфейс {@link ApiCallback}.
 */
public class AuthService {

    // --- Константы и поля ---
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    //================================================================================
    // Публичные методы API
    //================================================================================

    /**
     * Асинхронно отправляет запрос на регистрацию нового пользователя.
     * @param request DTO с данными для регистрации (имя, email, пароль).
     * @param callback Объект для обработки результата (успех, ошибка сети, ошибка сервера).
     */
    public void register(RegistrationRequest request, ApiCallback<UserDto> callback) {
        try {
            String jsonRequest = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(jsonRequest, JSON);
            Request httpRequest = new Request.Builder().url(BASE_URL + "/register").post(body).build();

            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response, UserDto.class, callback);
                }
            });
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    /**
     * Асинхронно отправляет запрос на аутентификацию (вход) пользователя.
     * @param request DTO с учетными данными (email, пароль).
     * @param callback Объект для обработки результата, который в случае успеха получит {@link AuthResponse}.
     */
    public void login(AuthRequest request, ApiCallback<AuthResponse> callback) {
        try {
            String jsonRequest = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(jsonRequest, JSON);
            Request httpRequest = new Request.Builder().url(BASE_URL + "/login").post(body).build();

            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response, AuthResponse.class, callback);
                }
            });
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Универсальный обработчик ответов от сервера для асинхронных вызовов.
     * Парсит тело ответа в нужный DTO в случае успеха или в {@link ErrorResponseDto} в случае ошибки.
     */
    private <T> void handleResponse(Response response, Class<T> clazz, ApiCallback<T> callback) {
        try (ResponseBody responseBody = response.body()) {
            String bodyString = responseBody.string();
            if (response.isSuccessful()) {
                T responseObject = objectMapper.readValue(bodyString, clazz);
                callback.onSuccess(responseObject);
            } else {

                try {
                    ErrorResponseDto errorDto = objectMapper.readValue(bodyString, ErrorResponseDto.class);
                    callback.onError(response.code(), errorDto.getMessage());
                } catch (Exception e) {
                    System.err.println("Ошибка парсинга json");
                    e.printStackTrace();
                    callback.onError(response.code(), bodyString);
                }
            }
        } catch (IOException e){
            callback.onFailure(e);
        }
    }
}