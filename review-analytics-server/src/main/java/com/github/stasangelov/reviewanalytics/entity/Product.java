package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность, представляющая товар в интернет-магазине, для которого оставляют отзывы.
 */
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
