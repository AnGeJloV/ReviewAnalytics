package com.github.stasangelov.reviewanalytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private Long productId;
    private String productName;
    private double averageRating;
}
