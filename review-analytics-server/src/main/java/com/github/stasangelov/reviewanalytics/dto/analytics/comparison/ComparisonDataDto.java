package com.github.stasangelov.reviewanalytics.dto.analytics.comparison;

import lombok.Data;
import java.util.List;

/**
 * DTO, содержащий данные одного товара для страницы сравнительного анализа.
 */
@Data
public class ComparisonDataDto {
    private Long productId;
    private String productName;
    private List<CriteriaProfileDto> criteriaProfile;
}
