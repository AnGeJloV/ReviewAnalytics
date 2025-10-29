package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
            // TODO: В будущем можно добавить фильтры по brandId, productId
    ) {
        // Передаем полученные параметры в сервис
        return ResponseEntity.ok(analyticsService.getDashboardData(startDate, endDate, categoryId));
    }
}