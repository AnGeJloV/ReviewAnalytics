package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.dto.ProductDetailsDto;
import com.github.stasangelov.reviewanalytics.dto.ProductSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

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
}