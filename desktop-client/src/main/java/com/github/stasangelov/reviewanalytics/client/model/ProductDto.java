package com.github.stasangelov.reviewanalytics.client.model;

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
