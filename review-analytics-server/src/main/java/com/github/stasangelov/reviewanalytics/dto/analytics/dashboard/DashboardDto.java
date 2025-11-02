package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.Data;
import java.util.List;

/**
 * Комплексный DTO, агрегирующий все данные для главной информационной панели.
 */
@Data
public class DashboardDto {
    private KpiDto kpis;
    private List<TopProductDto> topRatedProducts;
    private List<TopProductDto> worstRatedProducts;
    private List<CategoryRatingDto> categoryRatings;
    private List<RatingDynamicDto> ratingDynamics;
    private List<BrandRatingDto> brandRatings;
    private List<RatingDistributionDto> ratingDistribution;
}