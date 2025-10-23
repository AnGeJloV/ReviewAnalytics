package com.github.stasangelov.reviewanalytics.client.model;

import lombok.Data;

/**
 * DTO для сущности "Категория".
 * Используется для создания и обновления категорий.
 */

@Data
public class CategoryDto {
    private Long id;
    private String name;
}
