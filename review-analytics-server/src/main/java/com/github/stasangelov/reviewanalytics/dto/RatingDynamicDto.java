package com.github.stasangelov.reviewanalytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDynamicDto {
    private LocalDate date;
    private Double averageRating;
}
