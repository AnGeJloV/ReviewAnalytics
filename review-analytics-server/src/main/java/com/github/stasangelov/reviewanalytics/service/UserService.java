package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.AuthResponse;
import com.github.stasangelov.reviewanalytics.dto.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.entity.User;

/**
 * Сервис для управления пользователями.
 * Определяет контракт для регистрации, поиска и других операций с пользователями.
 */

public interface UserService {
    User registerUser(RegistrationRequest request);
    AuthResponse loginUser(AuthRequest authRequest);
}
