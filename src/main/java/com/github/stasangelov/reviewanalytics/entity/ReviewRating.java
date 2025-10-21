package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность, представляющая конкретную оценку по одному критерию в рамках одного отзыва.
 * Является связующим звеном между {@link Review} и {@link Criterion}.
 */

@Data
@Entity
@Table(name = "review_ratings")
public class ReviewRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // отзыв, к которому относится оценка

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id",  nullable = false)
    private Criterion criterion; // критерий, по которому поставлена оценка

    @Column(nullable = false)
    private Integer rating; // числовая оценка
}
