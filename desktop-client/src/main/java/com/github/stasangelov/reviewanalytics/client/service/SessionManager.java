package com.github.stasangelov.reviewanalytics.client.service;

import java.util.Collections;
import java.util.Set;

/**
 * Менеджер сессии, реализованный как синглтон (Singleton).
 * Его задача — хранить JWT-токен аутентифицированного пользователя в одном
 * глобально доступном месте. Это позволяет любому другому сервису или компоненту
 * приложения получить токен для добавления в заголовки защищенных запросов.
 */

public class SessionManager {
    private static SessionManager instance;
    private String token;
    private Set<String> roles = Collections.emptySet();

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getToken() {
        if (token == null) {
            return null;
        }
        return "Bearer " + token;
    }

    public void createSession(String token, Set<String> roles) {
        this.token = token;
        this.roles = (roles != null) ? roles : Collections.emptySet();
    }

    public boolean hasRole(String roleName) {
        return this.roles.contains(roleName);
    }

    public void clearSession() {
        this.token = null;
        this.roles = Collections.emptySet();
    }
}