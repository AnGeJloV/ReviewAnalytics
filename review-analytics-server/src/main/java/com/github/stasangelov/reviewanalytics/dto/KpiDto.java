package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

@Data
public class KpiDto {
    private long totalReviews;
    private double averageIntegralRating;
}
