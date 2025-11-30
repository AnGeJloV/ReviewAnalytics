package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.auth.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserServiceImpl userService;

    @Test
    @DisplayName("Ошибка регистрации: Email уже занят")
    void registerUser_ShouldThrowException_WhenEmailExists() {
        // 1. Данные
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@test.com");

        // 2. Мок: говорим, что такой юзер уже есть
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        // 3. Проверка
        assertThrows(RuntimeException.class, () -> userService.registerUser(req));
    }
}