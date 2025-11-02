package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для доступа к данным сущности {@link Review}.
 * Позволяет управлять отзывами в базе данных.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    /**
     * Находит все отзывы, у которых поле integralRating не заполнено (равно NULL).
     * Spring Data JPA автоматически сгенерирует нужный SQL-запрос по имени метода.
     */
    List<Review> findByIntegralRatingIsNull();
}
