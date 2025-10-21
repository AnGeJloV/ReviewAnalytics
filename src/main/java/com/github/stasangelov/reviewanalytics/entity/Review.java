package com.github.stasangelov.reviewanalytics.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность, представляющая один отзыв на товар.
 * Является центральной сущностью для анализа.
 */

@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "integral_rating")
    private Integer integralRating; // Общий рейтинг

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReviewStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewRating> reviewRatings;

    public enum ReviewStatus {
        ACTIVE,
        REJECTED
    }
}
