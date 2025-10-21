package com.github.stasangelov.reviewanalytics.config;

import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

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
    }
}