package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;
import java.util.List;

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
