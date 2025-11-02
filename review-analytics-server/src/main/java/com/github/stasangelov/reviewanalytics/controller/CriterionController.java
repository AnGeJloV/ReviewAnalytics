package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.dictionary.CriterionDto;
import com.github.stasangelov.reviewanalytics.service.CriterionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления критериями оценки.
 * Предоставляет эндпоинты для получения полного списка критериев,
 * а также для их фильтрации по категориям.
 */
@RestController
@RequestMapping("/api/criteria")
@RequiredArgsConstructor
public class CriterionController {
    private final CriterionService criterionService;

    /**
     * Возвращает полный список всех существующих критериев оценки.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<CriterionDto>> getAllCriteria() {
        return ResponseEntity.ok(criterionService.getAll());
    }

    /**
     * Возвращает список критериев, применимых к конкретной категории товара.
     */
    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<CriterionDto>> getCriteriaByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(criterionService.getByCategoryId(categoryId));
    }
}