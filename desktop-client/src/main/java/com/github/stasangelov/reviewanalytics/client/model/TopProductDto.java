package com.github.stasangelov.reviewanalytics.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopProductDto {
    private Long productId;
    private String productName;
    private double averageRating;
}
