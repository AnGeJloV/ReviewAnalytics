package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

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
