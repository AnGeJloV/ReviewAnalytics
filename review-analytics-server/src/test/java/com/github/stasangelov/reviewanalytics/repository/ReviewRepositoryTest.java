package com.github.stasangelov.reviewanalytics.repository;

import com.github.stasangelov.reviewanalytics.entity.Review;
import com.github.stasangelov.reviewanalytics.entity.Review.ReviewStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("Поиск в БД: Найти отзывы без рейтинга")
    void findByIntegralRatingIsNull_ShouldReturnReviewsWithoutRating() {
        // 1. Создаем и сохраняем отзыв без рейтинга (null)
        Review review = new Review();
        review.setDateCreated(LocalDateTime.now());
        review.setStatus(ReviewStatus.ACTIVE);
        review.setIntegralRating(null); // Явно null

        reviewRepository.save(review);

        // 2. Вызываем тестируемый метод репозитория
        List<Review> foundReviews = reviewRepository.findByIntegralRatingIsNull();

        // 3. Проверяем результат
        assertThat(foundReviews).isNotEmpty();
        assertThat(foundReviews.get(0).getIntegralRating()).isNull();
    }
}