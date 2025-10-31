package com.github.stasangelov.reviewanalytics.client.model.analytics.comparison;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO, содержащий данные одного товара, необходимые для страницы сравнительного анализа.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComparisonDataDto {
    private Long productId;
    private String productName;
    private List<CriteriaProfileDto> criteriaProfile;
}
