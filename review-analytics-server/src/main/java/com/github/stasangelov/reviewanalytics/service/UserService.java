package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.auth.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.auth.AuthResponse;
import com.github.stasangelov.reviewanalytics.dto.auth.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.entity.User;

/**
 * Сервис для управления пользователями.
 * Определяет контракт для регистрации, поиска и других операций с пользователями.
 */
public interface UserService {
    User registerUser(RegistrationRequest request);
    AuthResponse loginUser(AuthRequest authRequest);
}
