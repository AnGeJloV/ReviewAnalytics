package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.review.ReviewDto;
import com.github.stasangelov.reviewanalytics.entity.Criterion;
import com.github.stasangelov.reviewanalytics.entity.Product;
import com.github.stasangelov.reviewanalytics.entity.Review;
import com.github.stasangelov.reviewanalytics.repository.CriterionRepository;
import com.github.stasangelov.reviewanalytics.repository.ProductRepository;
import com.github.stasangelov.reviewanalytics.repository.ReviewRepository;
import com.github.stasangelov.reviewanalytics.service.mapper.ReviewMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CriterionRepository criterionRepository;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("Расчет рейтинга: (5*0.5 + 3*0.5) / 1 = 4.0")
    void create_ShouldCalculateIntegralRatingCorrectly() {
        // 1. Данные
        Long prodId = 1L;
        ReviewDto dto = new ReviewDto();
        dto.setProductId(prodId);
        dto.setDateCreated(LocalDateTime.now());
        Map<Long, Integer> ratings = new HashMap<>();
        ratings.put(10L, 5); // Оценка 5, вес 0.5
        ratings.put(20L, 3); // Оценка 3, вес 0.5
        dto.setRatings(ratings);

        // 2. Моки
        when(productRepository.findById(prodId)).thenReturn(Optional.of(new Product()));

        Criterion c1 = new Criterion(); c1.setWeight(0.5);
        Criterion c2 = new Criterion(); c2.setWeight(0.5);
        when(criterionRepository.findById(10L)).thenReturn(Optional.of(c1));
        when(criterionRepository.findById(20L)).thenReturn(Optional.of(c2));

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        // 3. Действие
        reviewService.create(dto);

        // 4. Проверка: (5*0.5 + 3*0.5) = 2.5 + 1.5 = 4.0
        verify(reviewRepository).save(org.mockito.ArgumentMatchers.argThat(r ->
                r.getIntegralRating() == 4.0
        ));
    }
}