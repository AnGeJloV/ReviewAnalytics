package com.github.stasangelov.reviewanalytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandRatingDto {
    private String brandName;
    private Double averageRating;
}
