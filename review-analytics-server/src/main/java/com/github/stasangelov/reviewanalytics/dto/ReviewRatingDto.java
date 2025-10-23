package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;

@Data
public class ReviewRatingDto {
    private Long criterionId;
    private String criterionName;
    private Integer rating;
}