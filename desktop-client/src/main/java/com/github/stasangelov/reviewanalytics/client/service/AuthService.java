package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.AuthRequest;
import com.github.stasangelov.reviewanalytics.client.model.AuthResponse;
import com.github.stasangelov.reviewanalytics.client.model.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.client.model.UserDto;
import okhttp3.*;
import java.io.IOException;

/**
 * Сервисный класс, инкапсулирующий всю логику взаимодействия с API аутентификации на сервере.
 * Предоставляет методы для регистрации ({@code register}) и входа ({@code login}).
 * Отвечает за создание HTTP-запросов, их асинхронную отправку, а также за парсинг (десериализацию)
 * JSON-ответов с помощью библиотеки Jackson.
 */

public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

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

    private <T> void handleResponse(Response response, Class<T> clazz, ApiCallback<T> callback) throws IOException {
        String responseBody = response.body().string();
        if (response.isSuccessful()) {
            T responseObject = objectMapper.readValue(responseBody, clazz);
            callback.onSuccess(responseObject);
        } else {
            callback.onError(response.code(), responseBody);
        }
    }
}