package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи агрегированной информации о среднем рейтинге по категории.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRatingDto {
    private String categoryName;
    private Double averageRating;
}