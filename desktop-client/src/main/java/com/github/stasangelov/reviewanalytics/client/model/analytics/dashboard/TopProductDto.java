package com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления информации о товаре в списках "топ лучших/худших".
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopProductDto {
    private Long productId;
    private String productName;
    private double averageRating;
}
