package com.github.stasangelov.reviewanalytics.client.model.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления одной оценки по одному критерию в рамках отзыва.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewRatingDto {
    private Long criterionId;
    private String criterionName;
    private Integer rating;
}