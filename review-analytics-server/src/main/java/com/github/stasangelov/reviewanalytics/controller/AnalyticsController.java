package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.dto.ProductDetailsDto;
import com.github.stasangelov.reviewanalytics.dto.ProductSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.github.stasangelov.reviewanalytics.dto.ComparisonDataDto;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')") // Доступен обеим ролям
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Возвращает все агрегированные данные для главной информационной панели.
     * Теперь принимает необязательные параметры для фильтрации.
     * @param startDate Начальная дата периода (формат yyyy-MM-dd).
     * @param endDate Конечная дата периода (формат yyyy-MM-dd).
     * @param categoryId ID категории для фильтрации.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId
    ) {
        // Передаем полученные параметры в сервис
        return ResponseEntity.ok(analyticsService.getDashboardData(startDate, endDate, categoryId));
    }
    /**
     * НОВЫЙ ЭНДПОИНТ: Возвращает сводную информацию по товарам для таблицы.
     */
    @GetMapping("/products-summary")
    public ResponseEntity<List<ProductSummaryDto>> getProductsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(analyticsService.getProductsSummary(startDate, endDate, categoryId));
    }

    /**
     * НОВЫЙ ЭНДПОИНТ: Возвращает детализированную информацию по одному товару.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDetailsDto> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(analyticsService.getProductDetails(productId));
    }
    /**
     * НОВЫЙ ЭНДПОИНТ: Принимает список ID и возвращает данные для сравнения.
     * Используем POST, так как передача списка ID в GET-запросе может быть неудобной.
     */
    @PostMapping("/compare")
    public ResponseEntity<List<ComparisonDataDto>> getComparisonData(@RequestBody List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(analyticsService.getComparisonData(productIds));
    }
}