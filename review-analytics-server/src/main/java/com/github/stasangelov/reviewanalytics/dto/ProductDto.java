package com.github.stasangelov.reviewanalytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для сущности "Товар".
 * Используется для создания, обновления и отображения товаров.
 */

@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "Название товара не может быть пустым")
    private String name;

    private String description;

    @NotBlank(message = "Бренд не может быть пустым")
    private String brand;

    @NotNull(message = "Категория должна быть указана")
    private Long categoryId;

    private String categoryName;
}
