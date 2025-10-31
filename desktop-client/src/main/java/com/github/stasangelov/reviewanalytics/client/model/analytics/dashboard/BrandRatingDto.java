package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO (Data Transfer Object) для передачи агрегированной информации о среднем рейтинге бренда.
 * Используется для отображения данных на дашборде, например, в BarChart.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandRatingDto {
    private String brandName;
    private Double averageRating;
}
