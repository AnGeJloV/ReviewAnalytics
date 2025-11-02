package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.auth.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.auth.AuthResponse;
import com.github.stasangelov.reviewanalytics.dto.auth.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.dto.user.UserDto;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для публичных эндпоинтов аутентификации и регистрации.
 * Этот контроллер обрабатывает запросы, которые не требуют JWT-токена.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * Регистрирует нового пользователя в системе.
     * Принимает данные для регистрации и валидирует их.
     * В случае успеха возвращает "безопасный" DTO пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegistrationRequest request) {
        // 1. Вызываем сервис для выполнения бизнес-логики регистрации
        User registeredUser = userService.registerUser(request);

        // 2. Конвертируем сущность User в "безопасный" UserDto для ответа
        UserDto responseDto = mapUserToUserDto(registeredUser);

        // 3. Возвращаем ответ клиенту
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Аутентифицирует пользователя по email и паролю.
     * В случае успеха возвращает JWT-токен и роли пользователя.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = userService.loginUser(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Преобразует сущность {@link User} в "безопасный" {@link UserDto}.
     * Используется, чтобы не отправлять на клиент хэш пароля и другую служебную информацию.
     */
    private UserDto mapUserToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}