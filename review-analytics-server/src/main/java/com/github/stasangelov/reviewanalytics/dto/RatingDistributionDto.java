package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

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
