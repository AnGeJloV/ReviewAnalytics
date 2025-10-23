package com.github.stasangelov.reviewanalytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для сущности "Категория".
 * Используется для создания и обновления категорий.
 */

@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Название категории не может быть пустым")
    private String name;
}
