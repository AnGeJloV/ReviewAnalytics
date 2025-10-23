package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.ReviewDto;
import com.github.stasangelov.reviewanalytics.dto.ReviewRatingDto;
import com.github.stasangelov.reviewanalytics.entity.*;
import com.github.stasangelov.reviewanalytics.exception.ResourceNotFoundException;
import com.github.stasangelov.reviewanalytics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .orElseThrow(() -> new ResourceNotFoundException("Товар не найден"));

        Review review = new Review();
        review.setProduct(product);
        review.setDateCreated(reviewDto.getDateCreated() != null ? reviewDto.getDateCreated() : LocalDateTime.now());
        review.setStatus(Review.ReviewStatus.ACTIVE); // По умолчанию делаем активным

        List<ReviewRating> ratings = reviewDto.getRatings().entrySet().stream().map(entry -> {
            Criterion criterion = criterionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Критерий не найден"));
            ReviewRating rating = new ReviewRating();
            rating.setReview(review);
            rating.setCriterion(criterion);
            rating.setRating(entry.getValue());
            return rating;
        }).collect(Collectors.toList());

        review.setReviewRatings(ratings);
        // TODO: Здесь нужно будет добавить вызов расчета интегрального рейтинга

        return toDto(reviewRepository.save(review));
    }

    // TODO: Добавить методы update, changeStatus

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setDateCreated(review.getDateCreated());
        dto.setStatus(review.getStatus());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());

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