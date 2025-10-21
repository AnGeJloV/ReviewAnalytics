package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.RoleRepository;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import com.github.stasangelov.reviewanalytics.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

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
    public String loginUser(AuthRequest authRequest){
        // 1. Spring Security проверяет правильность email и пароля
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );
        // 2. Если все верно, помещаем объект аутентификации в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Генерируем и возвращаем JWT-токен
        return jwtTokenProvider.createToken(authRequest.getEmail());
    }
}
