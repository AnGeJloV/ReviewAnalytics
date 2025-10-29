package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

/**
 * DTO для представления одной оценки в рамках отзыва.
 * Используется для чтения.
 */
@Data
public class ReviewRatingDto {
    private Long criterionId;
    private String criterionName;
    private Integer rating;
}