package com.github.stasangelov.reviewanalytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brand;
    private Long reviewCount;
    private Double averageRating;
}
