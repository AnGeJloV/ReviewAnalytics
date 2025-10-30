package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSummaryDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brand;
    private Long reviewCount;
    private Double averageRating;
}
