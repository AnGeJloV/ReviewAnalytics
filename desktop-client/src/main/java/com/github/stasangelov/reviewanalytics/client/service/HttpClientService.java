package com.github.stasangelov.reviewanalytics.client.service;

import lombok.Getter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Сервис-фабрика для создания OkHttpClient.
 * Гарантирует, что каждый запрос будет содержать JWT-токен для аутентификации,
 * который берется из SessionManager.
 */
public class HttpClientService {
    @Getter
    private static final OkHttpClient client;

    // Статический блок инициализации. Выполняется один раз при загрузке класса.
    static {
        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    /**
     * Внутренний класс-перехватчик. Его задача - "поймать" каждый исходящий запрос,
     * добавить в него заголовок Authorization с токеном и отправить дальше.
     */
    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder requestBuilder = chain.request().newBuilder();
            String token = SessionManager.getInstance().getToken(); // Получаем токен из сессии
            if (token != null && !token.isBlank() && !"Bearer null".equals(token)) {
                // Если токен есть, добавляем заголовок
                requestBuilder.addHeader("Authorization", token);
            }
            return chain.proceed(requestBuilder.build());
        }
    }
}