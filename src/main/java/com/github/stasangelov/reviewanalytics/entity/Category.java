package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность, представляющая категорию товара (например, "Ноутбуки", "Смартфоны").
 */

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
