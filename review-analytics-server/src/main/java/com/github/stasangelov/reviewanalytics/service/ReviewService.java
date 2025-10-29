package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.ReviewDto;
import com.github.stasangelov.reviewanalytics.dto.ReviewRatingDto;
import com.github.stasangelov.reviewanalytics.entity.*;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
import com.github.stasangelov.reviewanalytics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CriterionRepository criterionRepository;

    @Transactional(readOnly = true)
    public List<ReviewDto> getAll() {
        return reviewRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReviewDto create(ReviewDto reviewDto) {
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Товар с id " + reviewDto.getProductId() + " не найден"));

        Review review = new Review();
        review.setProduct(product);
        review.setDateCreated(reviewDto.getDateCreated());
        review.setStatus(Review.ReviewStatus.ACTIVE); // При ручном добавлении сразу делаем активным

        List<ReviewRating> ratings = reviewDto.getRatings().entrySet().stream().map(entry -> {
            Criterion criterion = criterionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Критерий с id " + entry.getKey() + " не найден"));
            ReviewRating rating = new ReviewRating();
            rating.setReview(review);
            rating.setCriterion(criterion);
            rating.setRating(entry.getValue());
            return rating;
        }).collect(Collectors.toList());

        review.setReviewRatings(ratings);
        review.setIntegralRating(calculateIntegralRating(review));

        return toDto(reviewRepository.save(review));
    }

    public ReviewDto update(Long id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв с id " + id + " не найден"));
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Товар с id " + reviewDto.getProductId() + " не найден"));

        review.setProduct(product);
        review.setDateCreated(reviewDto.getDateCreated());

        // Полностью заменяем старые оценки на новые
        review.getReviewRatings().clear();
        List<ReviewRating> newRatings = reviewDto.getRatings().entrySet().stream().map(entry -> {
            Criterion criterion = criterionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Критерий с id " + entry.getKey() + " не найден"));
            ReviewRating rating = new ReviewRating();
            rating.setReview(review);
            rating.setCriterion(criterion);
            rating.setRating(entry.getValue());
            return rating;
        }).collect(Collectors.toList());
        review.getReviewRatings().addAll(newRatings);

        review.setIntegralRating(calculateIntegralRating(review));

        return toDto(reviewRepository.save(review));
    }

    public ReviewDto changeStatus(Long id, Review.ReviewStatus newStatus) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв с id " + id + " не найден"));
        review.setStatus(newStatus);

        // TODO: Вызвать пересчет общей аналитики для товара, т.к. один отзыв был исключен/включен

        return toDto(reviewRepository.save(review));
    }
    /**
     * Рассчитывает взвешенный интегральный рейтинг для одного отзыва.
     * Формула: (Сумма(оценка * вес)) / (Сумма(весов))
     * @param review Сущность отзыва с его оценками.
     * @return Целочисленное значение рейтинга (в примере округляем до целого).
     */
    private Double calculateIntegralRating(Review review) {
        if (review.getReviewRatings() == null || review.getReviewRatings().isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (ReviewRating rating : review.getReviewRatings()) {
            double weight = rating.getCriterion().getWeight();
            weightedSum += rating.getRating() * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            return 0.0; // Избегаем деления на ноль
        }

        // Округляем до ближайшего целого. Можно использовать Math.round() для типа long.
        return weightedSum / totalWeight;
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setDateCreated(review.getDateCreated());
        dto.setStatus(review.getStatus());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setIntegralRating(review.getIntegralRating());

        List<ReviewRatingDto> ratingDtos = review.getReviewRatings().stream().map(rating -> {
            ReviewRatingDto ratingDto = new ReviewRatingDto();
            ratingDto.setCriterionId(rating.getCriterion().getId());
            ratingDto.setCriterionName(rating.getCriterion().getName());
            ratingDto.setRating(rating.getRating());
            return ratingDto;
        }).collect(Collectors.toList());
        dto.setReviewRatings(ratingDtos);

        return dto;
    }
}