package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')") // Доступен обеим ролям
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Возвращает все агрегированные данные для главной информационной панели.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboardData() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }
}