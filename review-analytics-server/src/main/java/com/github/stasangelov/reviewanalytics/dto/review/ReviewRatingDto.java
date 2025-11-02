package com.github.stasangelov.reviewanalytics.dto.review;

import lombok.Data;

/**
 * DTO для одной оценки по одному критерию в рамках отзыва.
 */
@Data
public class ReviewRatingDto {
    private Long criterionId;
    private String criterionName;
    private Integer rating;
}