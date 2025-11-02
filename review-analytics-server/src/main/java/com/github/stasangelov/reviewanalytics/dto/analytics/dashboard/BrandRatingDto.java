package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи агрегированной информации о среднем рейтинге бренда.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandRatingDto {
    private String brandName;
    private Double averageRating;
}
