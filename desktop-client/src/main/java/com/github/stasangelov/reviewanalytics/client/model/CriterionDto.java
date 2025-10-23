package com.github.stasangelov.reviewanalytics.client.model;

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
