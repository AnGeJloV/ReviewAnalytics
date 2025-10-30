package com.github.stasangelov.reviewanalytics.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComparisonDataDto {
    private Long productId;
    private String productName;
    private List<CriteriaProfileDto> criteriaProfile;
}
