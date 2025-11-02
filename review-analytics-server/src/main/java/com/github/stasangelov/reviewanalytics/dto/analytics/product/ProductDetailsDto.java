package com.github.stasangelov.reviewanalytics.dto.analytics.product;

import com.github.stasangelov.reviewanalytics.dto.analytics.comparison.CriteriaProfileDto;
import com.github.stasangelov.reviewanalytics.dto.review.ReviewDto;
import lombok.Data;
import java.util.List;

/**
 * Комплексный DTO со всей детализированной информацией о товаре.
 */
@Data
public class ProductDetailsDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brand;
    private Double averageRating;
    private Long reviewCount;
    private List<CriteriaProfileDto> criteriaProfile;
    private List<ReviewDto> reviews;
}
