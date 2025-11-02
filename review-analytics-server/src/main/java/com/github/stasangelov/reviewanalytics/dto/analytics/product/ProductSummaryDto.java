package com.github.stasangelov.reviewanalytics.dto.analytics.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO со сводной информацией о товаре для отображения в таблицах.
 */
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