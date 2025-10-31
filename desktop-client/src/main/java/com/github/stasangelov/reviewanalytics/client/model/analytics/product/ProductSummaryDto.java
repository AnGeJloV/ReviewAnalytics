package com.github.stasangelov.reviewanalytics.client.model.analytics.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO со сводной информацией о товаре.
 * Используется для отображения в главной таблице на дашборде.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSummaryDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brand;
    private Long reviewCount;
    private Double averageRating;
}
