package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KpiDto {
    private long totalReviews;
    private double averageIntegralRating;
}