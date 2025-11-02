package com.github.stasangelov.reviewanalytics.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для одной точки на графике динамики среднего рейтинга.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDynamicDto {
    private LocalDate date;
    private Double averageRating;
}