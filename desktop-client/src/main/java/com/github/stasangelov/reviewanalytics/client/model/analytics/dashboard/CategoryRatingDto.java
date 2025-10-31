package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для передачи агрегированной информации о среднем рейтинге по категории товаров.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryRatingDto {
    private String categoryName;
    private Double averageRating;
}
