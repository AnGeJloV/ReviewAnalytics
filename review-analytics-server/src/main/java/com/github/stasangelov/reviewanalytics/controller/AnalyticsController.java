package com.github.stasangelov.reviewanalytics.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.dto.analytics.dashboard.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.dto.analytics.product.ProductDetailsDto;
import com.github.stasangelov.reviewanalytics.dto.analytics.product.ProductSummaryDto;
import com.github.stasangelov.reviewanalytics.service.PdfGenerationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.github.stasangelov.reviewanalytics.dto.analytics.comparison.ComparisonDataDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * REST-контроллер для всех аналитических запросов.
 * Предоставляет эндпоинты для получения агрегированных данных,
 * детализации, сравнения, а также для генерации PDF-отчетов.
 * Доступен ролям ANALYST и ADMIN.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
public class AnalyticsController {

    // --- Поля и зависимости ---
    private final AnalyticsService analyticsService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;

    /**
     * Возвращает все агрегированные данные для главной информационной панели.
     * Принимает необязательные параметры для фильтрации.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(analyticsService.getDashboardData(startDate, endDate, categoryId));
    }

    /**
     * Возвращает сводную информацию по товарам для главной таблицы.
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
     * Возвращает детализированную информацию по одному товару.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDetailsDto> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(analyticsService.getProductDetails(productId));
    }

    /**
     * Принимает список ID товаров и возвращает данные для их сравнения.
     */
    @PostMapping("/compare")
    public ResponseEntity<List<ComparisonDataDto>> getComparisonData(@RequestBody List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(analyticsService.getComparisonData(productIds));
    }

    /**
     * Генерирует и возвращает PDF-отчет для главной информационной панели.
     * Принимает JSON с фильтрами и снимки графиков.
     */
    @PostMapping("/dashboard/export-pdf")
    public ResponseEntity<byte[]> exportDashboardPdf(
            @RequestPart("filters") String filtersJson,
            @RequestPart("charts") MultipartFile[] charts
    ) throws IOException {

        // 1. Десериализуем JSON с фильтрами обратно в объект
        DashboardFilters filters = objectMapper.readValue(filtersJson, DashboardFilters.class);

        // 2. Получаем данные для отчета на основе фильтров
        DashboardDto dashboardData = analyticsService.getDashboardData(
                filters.getStartDate(), filters.getEndDate(), filters.getCategoryId());

        // 3. Преобразуем MultipartFile[] в удобную Map<String, byte[]>
        Map<String, byte[]> chartImages = new HashMap<>();
        for (MultipartFile chart : charts) {
            // Убираем расширение .png из имени файла, чтобы оно соответствовало ключам
            String originalFilename = chart.getOriginalFilename().replace(".png", "");
            chartImages.put(originalFilename, chart.getBytes());
        }

        // 4. Генерируем PDF, передавая и данные, и изображения
        byte[] pdfContents = pdfGenerationService.generateDashboardPdf(dashboardData, chartImages);

        // 5. Формируем HTTP-ответ с файлом
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "dashboard_report.pdf");
        headers.setContentLength(pdfContents.length);

        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }

    /**
     * Генерирует и возвращает PDF-отчет для страницы детализации товара.
     */
    @PostMapping("/product/{productId}/export-pdf")
    public ResponseEntity<byte[]> exportProductDetailsPdf(
            @PathVariable Long productId,
            @RequestPart("chart") MultipartFile chart
    ) throws IOException {

        // 1. Получаем все данные по товару
        ProductDetailsDto detailsData = analyticsService.getProductDetails(productId);

        // 2. Генерируем PDF, передавая данные и снимок графика
        byte[] pdfContents = pdfGenerationService.generateProductDetailsPdf(detailsData, chart.getBytes());

        // 3. Формируем ответ
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String fileName = "product_report_" + productId + ".pdf";
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(pdfContents.length);

        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }

    /**
     * Генерирует и возвращает PDF-отчет для страницы сравнения.
     */
    @PostMapping("/compare/export-pdf")
    public ResponseEntity<byte[]> exportComparisonPdf(
            @RequestPart("productIds") String productIdsJson,
            @RequestPart("charts") MultipartFile[] charts
    ) throws IOException {

        List<Long> productIds = objectMapper.readValue(productIdsJson, new TypeReference<List<Long>>() {
        });
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ComparisonDataDto> comparisonData = analyticsService.getComparisonData(productIds);

        // Преобразуем массив файлов в удобную Map
        Map<String, byte[]> chartImages = new HashMap<>();
        for (MultipartFile chart : charts) {
            chartImages.put(chart.getOriginalFilename(), chart.getBytes());
        }

        // Передаем в сервис и данные, и Map с изображениями
        byte[] pdfContents = pdfGenerationService.generateComparisonPdf(comparisonData, chartImages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comparison_report.pdf");
        headers.setContentLength(pdfContents.length);

        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }

    /**
     * Внутренний класс для удобной десериализации фильтров из JSON.
     */
    @Data
    private static class DashboardFilters {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long categoryId;
    }
}