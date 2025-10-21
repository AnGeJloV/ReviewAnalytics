package com.github.stasangelov.reviewanalytics.controller;


import com.github.stasangelov.reviewanalytics.dto.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.dto.UserDto;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.security.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для обработки запросов, связанных с аутентификацией и регистрацией.
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody RegistrationRequest request) {
        // 1. Вызываем сервис для выполнения бизнес-логики регистрации
        User registeredUser = userService.registerUser(request);

        // 2. Конвертируем сущность User в "безопасный" UserDto для ответа
        UserDto responseDto = mapUserToUserDto(registeredUser);

        // 3. Возвращаем ответ клиенту
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Вспомогательный метод для преобразования сущности User в UserDto
     */

    private UserDto mapUserToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
