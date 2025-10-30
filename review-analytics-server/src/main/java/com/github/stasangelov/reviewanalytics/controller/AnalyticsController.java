package com.github.stasangelov.reviewanalytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.dto.DashboardDto;
import com.github.stasangelov.reviewanalytics.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.dto.ProductDetailsDto;
import com.github.stasangelov.reviewanalytics.dto.ProductSummaryDto;
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
import com.github.stasangelov.reviewanalytics.dto.ComparisonDataDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')") // Доступен обеим ролям
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;

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

    // Вспомогательный класс для десериализации фильтров
    @Data
    private static class DashboardFilters {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long categoryId;
    }
}