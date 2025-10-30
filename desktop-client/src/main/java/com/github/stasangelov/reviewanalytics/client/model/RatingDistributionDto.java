package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingDistributionDto {
    private String criterionName;
    private long rating1Count;
    private long rating2Count;
    private long rating3Count;
    private long rating4Count;
    private long rating5Count;
}
