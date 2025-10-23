package com.github.stasangelov.reviewanalytics.dto;

import com.github.stasangelov.reviewanalytics.entity.Review;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReviewDto {
    private Long id;
    private LocalDateTime dateCreated;
    private Review.ReviewStatus status;

    @NotNull(message = "Товар должен быть указан")
    private Long productId;
    private String productName;

    // Для создания/обновления: карта [criterionId -> rating]
    private Map<Long, Integer> ratings;

    // Для чтения: полный список оценок
    private List<ReviewRatingDto> reviewRatings;
}