package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDetailsDto {
    // Общая информация
    private Long productId;
    private String productName;
    private String categoryName;
    private String brand;
    private Double averageRating;
    private Long reviewCount;

    // Данные для RadarChart (профиль критериев)
    private List<CriteriaProfileDto> criteriaProfile;

    // Полный список отзывов на этот товар
    private List<ReviewDto> reviews;
}
