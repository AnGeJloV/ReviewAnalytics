package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.ReviewRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к данным сущности {@link ReviewRating} (оценок по критериям).
 */

@Repository
public interface ReviewRatingRepository extends JpaRepository<ReviewRating,Long> {
}
