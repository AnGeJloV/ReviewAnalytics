package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.dictionary.CategoryDto;
import com.github.stasangelov.reviewanalytics.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления категориями товаров.
 * Предоставляет эндпоинты для получения справочной информации о категориях.
 * Доступен ролям ANALYST и ADMIN.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Возвращает полный список всех категорий товаров.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }
}
