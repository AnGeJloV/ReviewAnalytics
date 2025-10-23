package com.github.stasangelov.reviewanalytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для сущности "Товар".
 */

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
}
