package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления товара в списках "топ лучших/худших".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private Long productId;
    private String productName;
    private double averageRating;
}
