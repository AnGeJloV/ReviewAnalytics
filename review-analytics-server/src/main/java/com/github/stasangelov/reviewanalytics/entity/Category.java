package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * Сущность, представляющая категорию товара (например, "Ноутбуки", "Смартфоны").
 */
@Data
@Entity
@Table(name = "categories")
@ToString(exclude = {"products", "criteria"})
@EqualsAndHashCode(exclude = {"products", "criteria"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_criteria",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "criterion_id")
    )
    private Set<Criterion> criteria;
}
