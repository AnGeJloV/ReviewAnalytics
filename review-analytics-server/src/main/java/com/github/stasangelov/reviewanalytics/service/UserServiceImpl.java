package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.AuthResponse;
import com.github.stasangelov.reviewanalytics.dto.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.exception.InvalidCredentialsException;
import com.github.stasangelov.reviewanalytics.repository.RoleRepository;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import com.github.stasangelov.reviewanalytics.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса {@link UserService}.
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User registerUser(RegistrationRequest request){
        // 1. Проверка, не занят ли email
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        // 2. Создаем новую сущность User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        // 3. Хешируем пароль
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        // 4. Назначаем роль по умолчанию
        Role userRole = roleRepository.findByName(Role.RoleName.ANALYST)
                .orElseThrow(() -> new RuntimeException("Роль по умолчанию ANALYST не найдена в БД"));
        user.setRoles(Set.of(userRole));

        // 5. Сохраняем пользователя в БД и возвращаем его
        return userRepository.save(user);
    }

    @Override
    public AuthResponse loginUser(AuthRequest authRequest) {
        try {
            // 1. Spring Security проверяет правильность email и пароля
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
            // 2. Если все верно, помещаем объект аутентификации в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Генерируем JWT-токен
            String token = jwtTokenProvider.createToken(authRequest.getEmail());

            // 4. Получаем роли пользователя из объекта Authentication
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // 5. Создаем и возвращаем объект AuthResponse
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRoles(roles);
            return response;

        } catch (AuthenticationException e) {
            // Если аутентификация провалилась, бросаем наше кастомное исключение
            throw new InvalidCredentialsException("Неправильный email или пароль");
        }
    }
}
