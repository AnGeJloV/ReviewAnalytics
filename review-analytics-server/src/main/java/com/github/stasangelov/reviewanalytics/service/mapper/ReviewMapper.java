package com.github.stasangelov.reviewanalytics.service.mapper;

import com.github.stasangelov.reviewanalytics.dto.ReviewDto;
import com.github.stasangelov.reviewanalytics.dto.ReviewRatingDto;
import com.github.stasangelov.reviewanalytics.entity.Review;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setDateCreated(review.getDateCreated());
        dto.setStatus(review.getStatus());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setIntegralRating(review.getIntegralRating());

        dto.setReviewRatings(review.getReviewRatings().stream().map(rating -> {
            ReviewRatingDto ratingDto = new ReviewRatingDto();
            ratingDto.setCriterionId(rating.getCriterion().getId());
            ratingDto.setCriterionName(rating.getCriterion().getName());
            ratingDto.setRating(rating.getRating());
            return ratingDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
