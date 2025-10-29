package com.github.stasangelov.reviewanalytics.dto;

import com.github.stasangelov.reviewanalytics.entity.Review;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Комплексный DTO для сущности "Отзыв".
 * Используется для всех операций: создание, обновление, чтение.
 */
@Data
public class ReviewDto {
    // Поля для чтения
    private Long id;
    private Review.ReviewStatus status;
    private String productName;
    private List<ReviewRatingDto> reviewRatings;

    // Поля для записи (создание/обновление)
    @NotNull(message = "Товар должен быть указан")
    private Long productId;

    @NotNull(message = "Дата должна быть указана")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreated;

    private Double integralRating;

    @NotEmpty(message = "Должна быть хотя бы одна оценка")
    private Map<Long, Integer> ratings; // Карта [criterionId -> rating]
}