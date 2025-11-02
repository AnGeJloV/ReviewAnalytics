package com.github.stasangelov.reviewanalytics.config;

import com.github.stasangelov.reviewanalytics.entity.Review;
import com.github.stasangelov.reviewanalytics.entity.ReviewRating;
import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.ReviewRepository;
import com.github.stasangelov.reviewanalytics.repository.RoleRepository;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Компонент, отвечающий за инициализацию базовых данных при старте приложения.
 * Реализует {@link CommandLineRunner}, что гарантирует выполнение метода {@code run}
 * после того, как контекст Spring Boot будет полностью загружен.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // --- Поля и зависимости ---
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;

    /**
     * Точка входа для инициализатора. Выполняется один раз при запуске приложения.
     * Метод создает роли и пользователей по умолчанию, а также выполняет
     * необходимые миграции данных, такие как пересчет рейтингов.
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Запуск инициализации системных данных...");

        // 1. Создаем роли, если их нет.
        Role adminRole = createRoleIfNotFound(Role.RoleName.ADMIN);
        Role analystRole = createRoleIfNotFound(Role.RoleName.ANALYST);

        // 2. Создаем пользователя-администратора, если его нет.
        createUserIfNotFound("admin", "admin@admin", "admin", Set.of(adminRole));

        // 3. Создаем обычного пользователя-аналитика, если его нет.
        createUserIfNotFound("user", "user@user", "user", new HashSet<>(Set.of(analystRole)));

        recalculateAllIntegralRatings();

        log.info("Инициализация системных данных завершена.");
    }

    /**
     * Ищет роль по имени в базе данных. Если роль не найдена, создает и сохраняет ее.
     * Этот метод гарантирует наличие системных ролей.
     */
    private Role createRoleIfNotFound(Role.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    log.info("Создана роль по умолчанию: {}", roleName);
                    return roleRepository.save(newRole);
                });
    }

    /**
     * Создает пользователя, если он с указанным email еще не существует в базе данных.
     * Пароль автоматически хешируется перед сохранением.
     */
    private void createUserIfNotFound(String name, String email, String password, Set<Role> roles) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setActive(true);
            user.setRoles(new HashSet<>(roles));

            userRepository.save(user);
            log.info("Создан пользователь по умолчанию: email='{}', password='{}'", email, password);
        } else {
            log.info("Пользователь с email='{}' уже существует. Пропускаем создание.", email);
        }
    }

    /**
     * Находит все отзывы с нерассчитанным интегральным рейтингом (NULL)
     * и вычисляет его значение. Полезно для миграции старых данных.
     */
    private void recalculateAllIntegralRatings() {
        log.info("Проверка и пересчет интегральных рейтингов...");
        // Находим все отзывы, где integral_rating IS NULL
        List<Review> reviewsToUpdate = reviewRepository.findByIntegralRatingIsNull();

        if (reviewsToUpdate.isEmpty()) {
            log.info("Все интегральные рейтинги уже рассчитаны.");
            return;
        }

        log.info("Найдено {} отзывов для пересчета рейтинга.", reviewsToUpdate.size());
        for (Review review : reviewsToUpdate) {
            // Для каждого отзыва вызываем наш уже существующий метод расчета
            review.setIntegralRating(calculateIntegralRating(review));
        }

        // Сохраняем все обновленные отзывы одним пакетом
        reviewRepository.saveAll(reviewsToUpdate);
        log.info("Пересчет рейтингов завершен.");
    }

    /**
     * Локальная копия метода расчета взвешенного интегрального рейтинга для отзыва.
     * Дублируется здесь, чтобы избежать циклической зависимости между сервисами и
     * компонентами конфигурации.
     */
    private Double calculateIntegralRating(Review review) {
        if (review.getReviewRatings() == null || review.getReviewRatings().isEmpty()) {
            return 0.0;
        }
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        for (ReviewRating rating : review.getReviewRatings()) {
            double weight = rating.getCriterion().getWeight();
            weightedSum += rating.getRating() * weight;
            totalWeight += weight;
        }
        if (totalWeight == 0) return 0.0;
        return weightedSum / totalWeight;
    }
}