package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для ключевых показателей эффективности (Key Performance Indicators).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KpiDto {
    private long totalReviews;
    private double averageIntegralRating;
}