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

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;

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
     * Вспомогательный метод, который ищет роль по имени. Если не находит - создает и сохраняет.
     * @param roleName Имя роли для создания/поиска.
     * @return Сущность Role (найденная или только что созданная).
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
     * Вспомогательный метод для создания пользователя, если он отсутствует в БД.
     * @param name Имя пользователя.
     * @param email Email (используется как логин).
     * @param password Пароль в открытом виде (будет захеширован).
     * @param roles Набор ролей, которые нужно присвоить пользователю.
     */
    private void createUserIfNotFound(String name, String email, String password, Set<Role> roles) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            // Обязательно хешируем пароль!
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
     * Новый метод для пересчета рейтингов.
     * Загружает все отзывы, у которых не посчитан рейтинг, и рассчитывает его.
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
     * Копия метода расчета из ReviewService.
     * Мы дублируем его здесь, чтобы не создавать циклическую зависимость
     * между DataInitializer и ReviewService.
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