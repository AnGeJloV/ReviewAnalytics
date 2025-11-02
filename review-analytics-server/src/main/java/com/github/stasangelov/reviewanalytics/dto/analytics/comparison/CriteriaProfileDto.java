package com.github.stasangelov.reviewanalytics.dto.analytics.comparison;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления среднего рейтинга по одному критерию.
 * Используется для построения профиля товара (например, для RadarChart).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaProfileDto {
    private String criterionName;
    private Double averageRating;
}