package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link Review}.
 * Позволяет управлять отзывами в базе данных.
 */

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
}
