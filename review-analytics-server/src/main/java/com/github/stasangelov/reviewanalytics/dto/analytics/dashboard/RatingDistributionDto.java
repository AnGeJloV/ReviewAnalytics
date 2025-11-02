package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.Data;

/**
 * DTO для данных о распределении оценок (1-5) по одному критерию.
 */
@Data
public class RatingDistributionDto {
    private String criterionName;
    private long rating1Count;
    private long rating2Count;
    private long rating3Count;
    private long rating4Count;
    private long rating5Count;

    public RatingDistributionDto(String criterionName) {
        this.criterionName = criterionName;
    }
}