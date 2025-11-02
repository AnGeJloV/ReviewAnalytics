package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.review.ReviewDto;
import com.github.stasangelov.reviewanalytics.entity.Review;
import com.github.stasangelov.reviewanalytics.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST-контроллер для управления отзывами.
 * Предоставляет полный набор CRUD-операций (создание, чтение, обновление),
 * а также эндпоинт для модерации (изменения статуса).
 * Доступен только пользователям с ролью ADMIN.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Возвращает полный список всех отзывов в системе.
     */
    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAll());
    }

    /**
     * Создает новый отзыв на основе переданных данных.
     */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto reviewDto) {
        return new ResponseEntity<>(reviewService.create(reviewDto), HttpStatus.CREATED);
    }

    /**
     * Полностью обновляет существующий отзыв по его ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.update(id, reviewDto));
    }

    /**
     * Частично обновляет отзыв, изменяя его статус (например, ACTIVE или REJECTED).
     * Ожидает JSON вида {"status": "ACTIVE"}.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReviewDto> changeReviewStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Review.ReviewStatus newStatus = Review.ReviewStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(reviewService.changeStatus(id, newStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}