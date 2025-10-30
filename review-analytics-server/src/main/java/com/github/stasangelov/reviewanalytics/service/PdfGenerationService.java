package com.github.stasangelov.reviewanalytics.service;

import com.github.stasangelov.reviewanalytics.dto.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfGenerationService {

    private PDType0Font font;
    private PDType0Font fontBold;

    // --- Утилитарные методы для генерации PDF ---

    private void loadFonts(PDDocument document) throws IOException {
        try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf");
             InputStream fontBoldStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans-Bold.ttf")) {
            if (fontStream == null || fontBoldStream == null) {
                throw new IOException("Не удалось найти файлы шрифтов в ресурсах.");
            }
            this.font = PDType0Font.load(document, fontStream);
            this.fontBold = PDType0Font.load(document, fontBoldStream);
        }
    }

    // --- Основной метод для генерации отчета по дашборду ---

    public byte[] generateDashboardPdf(DashboardDto data, Map<String, byte[]> chartImages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            loadFonts(document);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = 750;
            final float margin = 50;
            final float width = page.getMediaBox().getWidth() - 2 * margin;

            // --- Заголовок и дата ---
            addText(contentStream, fontBold, 18, margin, yPosition, "Отчет: Информационная панель");
            yPosition -= 15;
            String reportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            addText(contentStream, font, 10, margin, yPosition, "Дата формирования: " + reportDate);
            yPosition -= 30;

            // --- KPI ---
            addText(contentStream, fontBold, 14, margin, yPosition, "Ключевые показатели (KPI)");
            yPosition -= 20;
            addText(contentStream, font, 12, margin, yPosition, "Всего отзывов: " + data.getKpis().getTotalReviews());
            yPosition -= 20;
            addText(contentStream, font, 12, margin, yPosition, "Средний интегральный рейтинг: " + String.format("%.2f", data.getKpis().getAverageIntegralRating()));
            yPosition -= 30;

            // --- Таблицы Топ/Худших товаров ---
            yPosition = drawTable(document, contentStream, margin, yPosition, "Топ-5 лучших товаров",
                    List.of("Название товара", "Средний рейтинг"),
                    data.getTopRatedProducts().stream().map(p -> List.of(p.getProductName(), String.format("%.2f", p.getAverageRating()))).collect(Collectors.toList()),
                    new float[]{0.75f, 0.25f});
            yPosition -= 20;
            yPosition = drawTable(document, contentStream, margin, yPosition, "Топ-5 худших товаров",
                    List.of("Название товара", "Средний рейтинг"),
                    data.getWorstRatedProducts().stream().map(p -> List.of(p.getProductName(), String.format("%.2f", p.getAverageRating()))).collect(Collectors.toList()),
                    new float[]{0.75f, 0.25f});

            // --- Вставка изображений графиков ---
            contentStream.close(); // Закрываем текущий поток перед добавлением новых страниц/изображений

            // Добавляем каждый график на новой странице для наглядности
            if (chartImages.get("categoryChart") != null) {
                addChartToNewPage(document, "Средний рейтинг по категориям/брендам", chartImages.get("categoryChart"));
            }
            if (chartImages.get("dynamicsChart") != null) {
                addChartToNewPage(document, "Динамика среднего рейтинга", chartImages.get("dynamicsChart"));
            }
            if (chartImages.get("distributionChart") != null) {
                addChartToNewPage(document, "Распределение оценок по критериям", chartImages.get("distributionChart"));
            }

            // ... (здесь можно добавить таблицу ProductSummary, если нужно) ...

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void addChartToNewPage(PDDocument document, String title, byte[] imageBytes) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "chart");

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = 750;

            addText(contentStream, fontBold, 16, margin, yPosition, title);
            yPosition -= 30;

            // Масштабируем изображение, чтобы оно вписалось в страницу
            float pageWidth = page.getMediaBox().getWidth() - 2 * margin;
            float scale = pageWidth / pdImage.getWidth();
            float imgWidth = pdImage.getWidth() * scale;
            float imgHeight = pdImage.getHeight() * scale;

            contentStream.drawImage(pdImage, margin, yPosition - imgHeight, imgWidth, imgHeight);
        }
    }

    // --- Вспомогательные методы ---

    private void addText(PDPageContentStream stream, PDType0Font font, int fontSize, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private float drawTable(PDDocument doc, PDPageContentStream stream, float x, float y, String title, List<String> headers, List<List<String>> data, float[] colWidths) throws IOException {
        float tableTopY = y;
        float rowHeight = 20f;
        float tableWidth = PDRectangle.A4.getWidth() - 2 * x;
        float cellMargin = 5f;

        // Заголовок
        addText(stream, fontBold, 14, x, tableTopY, title);
        tableTopY -= 30;

        // Шапка таблицы
        float nextX = x;
        for (int i = 0; i < headers.size(); i++) {
            float width = tableWidth * colWidths[i];
            addText(stream, fontBold, 10, nextX + cellMargin, tableTopY - 15, headers.get(i));
            nextX += width;
        }

        // Линии
        stream.setLineWidth(0.5f);
        stream.moveTo(x, tableTopY);
        stream.lineTo(x + tableWidth, tableTopY);
        stream.stroke();
        tableTopY -= rowHeight;

        // Данные
        for (List<String> row : data) {
            nextX = x;
            for (int i = 0; i < row.size(); i++) {
                float width = tableWidth * colWidths[i];
                String text = row.get(i);
                // Простое усечение длинного текста
                if (text.length() > 50) text = text.substring(0, 47) + "...";
                addText(stream, font, 10, nextX + cellMargin, tableTopY - 15, text);
                nextX += width;
            }
            stream.moveTo(x, tableTopY);
            stream.lineTo(x + tableWidth, tableTopY);
            stream.stroke();
            tableTopY -= rowHeight;
        }
        return tableTopY;
    }

    // Заглушки для других отчетов
    public byte[] generateProductDetailsPdf(ProductDetailsDto data) { return new byte[0]; }
    public byte[] generateComparisonPdf(List<ComparisonDataDto> data) { return new byte[0]; }
}