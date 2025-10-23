package com.github.stasangelov.reviewanalytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для сущности "Категория".
 */

@Data
public class CategoryDto {
    private Long id;
    private String name;
}
