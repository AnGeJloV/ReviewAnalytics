package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.dictionary.ProductDto;
import com.github.stasangelov.reviewanalytics.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления товарами.
 * Предоставляет эндпоинты для получения справочной информации о товарах.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Возвращает полный список всех товаров в системе.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAll());
    }
}