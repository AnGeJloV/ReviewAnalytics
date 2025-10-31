package com.github.stasangelov.reviewanalytics.client.model.analytics.comparison;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления среднего рейтинга по одному конкретному критерию.
 * Используется для построения "профиля" сильных и слабых сторон товара.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriteriaProfileDto {
    private String criterionName;
    private Double averageRating;
}
