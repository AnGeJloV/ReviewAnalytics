package com.github.stasangelov.reviewanalytics.client.model.analytics.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.stasangelov.reviewanalytics.client.model.analytics.comparison.CriteriaProfileDto;
import com.github.stasangelov.reviewanalytics.client.model.review.ReviewDto;
import lombok.Data;
import java.util.List;

/**
 * Комплексный DTO, содержащий всю детализированную информацию о товаре.
 * Используется для отображения страницы "Детализация по товару".
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
