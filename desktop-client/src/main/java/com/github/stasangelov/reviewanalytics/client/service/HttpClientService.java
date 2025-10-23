package com.github.stasangelov.reviewanalytics.client.service;

import lombok.Getter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Сервис-фабрика для создания OkHttpClient.
 * Гарантирует, что каждый запрос будет содержать JWT-токен для аутентификации.
 */
public class HttpClientService {
    @Getter
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder requestBuilder = chain.request().newBuilder();
            String token = SessionManager.getInstance().getToken();
            if (token != null && !token.isBlank()) {
                requestBuilder.addHeader("Authorization", token);
            }
            return chain.proceed(requestBuilder.build());
        }
    }
}