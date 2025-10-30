package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComparisonDataDto {
    private Long productId;
    private String productName;
    private List<CriteriaProfileDto> criteriaProfile;
}
