package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Комплексный DTO, который агрегирует все данные, необходимые для
 * отображения главной информационной панели (дашборда).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardDto {
    private KpiDto kpis;
    private List<TopProductDto> topRatedProducts;
    private List<TopProductDto> worstRatedProducts;
    private List<CategoryRatingDto> categoryRatings;
    private List<RatingDynamicDto> ratingDynamics;
    private List<BrandRatingDto> brandRatings;
    private List<RatingDistributionDto> ratingDistribution;
}
