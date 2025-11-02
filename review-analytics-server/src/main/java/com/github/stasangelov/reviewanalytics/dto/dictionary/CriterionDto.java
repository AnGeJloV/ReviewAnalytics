package com.github.stasangelov.reviewanalytics.dto.dictionary;

import lombok.Data;

/**
 * DTO для сущности "Критерий оценки".
 */
@Data
public class CriterionDto {
    private Long id;
    private String name;
    private Double weight;
}