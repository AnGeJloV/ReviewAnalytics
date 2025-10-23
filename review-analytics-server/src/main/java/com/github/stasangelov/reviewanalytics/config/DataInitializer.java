package com.github.stasangelov.reviewanalytics.config;

import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.RoleRepository;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Компонент для инициализации базовых данных при первом запуске приложения.
 */

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, существует ли роль ANALYST
        if (roleRepository.findByName(Role.RoleName.ANALYST).isEmpty()) {
            Role analystRole = new Role();
            analystRole.setName(Role.RoleName.ANALYST);
            roleRepository.save(analystRole);
            System.out.println("Роль ANALYST создана.");
        }

        // Проверяем, существует ли роль ADMIN
        if (roleRepository.findByName(Role.RoleName.ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Роль ADMIN создана.");
        }

        // Добавляем админа по умолчанию
        String adminEmail = "admin@admin";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            // Если пользователя нет, то нам нужно получить сущности ролей из БД
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена. Невозможно создать администратора."));
            Role analystRole = roleRepository.findByName(Role.RoleName.ANALYST)
                    .orElseThrow(() -> new RuntimeException("Роль ANALYST не найдена. Невозможно создать администратора."));

            // Создаем нового пользователя
            User adminUser = new User();
            adminUser.setName("admin");
            adminUser.setEmail(adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode("admin"));
            adminUser.setActive(true);
            adminUser.setRoles(Set.of(adminRole, analystRole));

            userRepository.save(adminUser);
            System.out.println("Создан пользователь-администратор по умолчанию.");
            System.out.println("Email: " + adminEmail);
            System.out.println("Пароль: admin");
        }
    }
}