package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.Data;

/**
 * DTO для ключевых показателей эффективности (KPI).
 */
@Data
public class KpiDto {
    private long totalReviews;
    private double averageIntegralRating;
}
