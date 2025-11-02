package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.review.ReviewDto;
import com.github.stasangelov.reviewanalytics.dto.review.ReviewRatingDto;
import com.github.stasangelov.reviewanalytics.entity.*;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
import com.github.stasangelov.reviewanalytics.repository.*;
import com.github.stasangelov.reviewanalytics.service.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сущностями "Отзыв" ({@link Review}).
 * Предоставляет полную бизнес-логику для создания, чтения, обновления
 * и модерации отзывов. Все операции по умолчанию выполняются в одной транзакции.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CriterionRepository criterionRepository;
    private final ReviewMapper reviewMapper;

    /**
     * Возвращает список всех отзывов в системе.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getAll() {
        return reviewRepository.findAll().stream().map(reviewMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Создает новый отзыв, связывает его с товаром и критериями,
     * рассчитывает интегральный рейтинг и сохраняет в БД.
     */
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

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    /**
     * Обновляет существующий отзыв по его ID.
     * Заменяет старые оценки на новые и пересчитывает интегральный рейтинг.
     */
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

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    /**
     * Изменяет статус отзыва (модерация).
     */
    public ReviewDto changeStatus(Long id, Review.ReviewStatus newStatus) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв с id " + id + " не найден"));
        review.setStatus(newStatus);
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    /**
     * Рассчитывает взвешенный интегральный рейтинг для одного отзыва.
     * Формула: (Сумма(оценка * вес критерия)) / (Сумма(весов всех критериев)).
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
            return 0.0;
        }

        return weightedSum / totalWeight;
    }
}