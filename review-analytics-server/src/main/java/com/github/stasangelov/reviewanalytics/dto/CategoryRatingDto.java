package com.github.stasangelov.reviewanalytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRatingDto {
    private String categoryName;
    private Double averageRating;
}