package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.AuthRequest;
import com.github.stasangelov.reviewanalytics.dto.AuthResponse;
import com.github.stasangelov.reviewanalytics.dto.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.dto.UserManagementDto;
import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.exception.InvalidCredentialsException;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.stasangelov.reviewanalytics.exception.OperationForbiddenException;
import org.springframework.transaction.annotation.Transactional;

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
        user.setRoles(new HashSet<>(Set.of(userRole)));

        // 5. Сохраняем пользователя в БД и возвращаем его
        return userRepository.save(user);
    }

    @Override
    public AuthResponse loginUser(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            // Получаем сущность пользователя из БД и проверяем active
            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден"));

            if (!user.isActive()) {
                throw new InvalidCredentialsException("Учетная запись заблокирована");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.createToken(authRequest.getEmail());

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRoles(roles);
            return response;

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Неправильный email или пароль");
        }
    }

    @Transactional(readOnly = true)
    public List<UserManagementDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserManagementDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.isActive(),
                        user.getRoles()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserManagementDto changeUserRole(Long userId, Role.RoleName newRoleName, User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw new OperationForbiddenException("Вы не можете изменить собственную роль.");
        }

        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        Role newRole = roleRepository.findByName(newRoleName)
                .orElseThrow(() -> new RuntimeException("Роль " + newRoleName + " не найдена"));

        // Устанавливаем только одну роль
        userToUpdate.setRoles(new HashSet<>(Set.of(newRole)));
        User updatedUser = userRepository.save(userToUpdate);

        return new UserManagementDto(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(), updatedUser.isActive(), updatedUser.getRoles());
    }

    @Transactional
    public UserManagementDto changeUserStatus(Long userId, boolean newStatus, User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw new OperationForbiddenException("Вы не можете заблокировать собственную учетную запись.");
        }

        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        userToUpdate.setActive(newStatus);
        User updatedUser = userRepository.save(userToUpdate);

        return new UserManagementDto(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(), updatedUser.isActive(), updatedUser.getRoles());
    }
}
