package com.github.stasangelov.reviewanalytics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * Сущность, представляющая критерий оценки в отзыве (например, "Качество сборки", "Цена").
 */

@Data
@Entity
@Table(name = "criteria")
@ToString(exclude = "categories")
@EqualsAndHashCode(exclude = "categories")
public class Criterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Double weight; // Весовой коэффициент

    /**
     * Обратная связь на категории, к которым относится этот критерий.
     * `mappedBy` указывает, что главная сторона связи находится в поле 'criteria' класса Category.
     */
    @ManyToMany(mappedBy = "criteria", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Category> categories;
}
