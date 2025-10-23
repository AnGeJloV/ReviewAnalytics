package com.github.stasangelov.reviewanalytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для сущности "Критерий оценки".
 * Используется для создания и обновления критериев.
 */

@Data
public class CriterionDto {
    private Long id;

    @NotBlank(message = "Название критерия не может быть пустым")
    private String name;

    @NotBlank(message = "Весовой коэффициент не может быть пустым")
    private Double weight;
}
