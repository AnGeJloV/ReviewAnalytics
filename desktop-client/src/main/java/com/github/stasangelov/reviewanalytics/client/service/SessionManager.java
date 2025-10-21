package com.github.stasangelov.reviewanalytics.client.service;

/**
 * Менеджер сессии, реализованный как синглтон (Singleton).
 * Его задача — хранить JWT-токен аутентифицированного пользователя в одном
 * глобально доступном месте. Это позволяет любому другому сервису или компоненту
 * приложения получить токен для добавления в заголовки защищенных запросов.
 */

public class SessionManager {
    private static SessionManager instance;
    private String token;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getToken() {
        return "Bearer " + token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void clearSession() {
        this.token = null;
    }
}