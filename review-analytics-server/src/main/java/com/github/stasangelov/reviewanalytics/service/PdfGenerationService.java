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
import java.util.ArrayList;
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
        final PDPage page = doc.getPage(doc.getNumberOfPages() - 1);
        float tableTopY = y;
        final float rowHeight = 20f;
        final float tableWidth = page.getMediaBox().getWidth() - 2 * x;
        final float cellMargin = 5f;
        final int headerFontSize = 9;
        final int cellFontSize = 10;

        // Заголовок
        addText(stream, fontBold, 14, x, tableTopY, title);
        tableTopY -= 30;


        final float headerY = tableTopY - 15;
        float nextX = x;
        float maxHeaderHeight = 0;

        List<List<String>> wrappedHeaders = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            float colWidth = tableWidth * colWidths[i];
            // Используем обычный шрифт (font) вместо жирного (fontBold)
            List<String> lines = splitTextIntoLines(headers.get(i), font, headerFontSize, colWidth - 2 * cellMargin);
            wrappedHeaders.add(lines);
            if (lines.size() * (headerFontSize + 2f) > maxHeaderHeight) {
                maxHeaderHeight = lines.size() * (headerFontSize + 2f);
            }
        }

        // Рисуем текст заголовков с центрированием
        for (int i = 0; i < wrappedHeaders.size(); i++) {
            float colWidth = tableWidth * colWidths[i];
            List<String> lines = wrappedHeaders.get(i);

            // Вычисляем начальную Y-позицию, чтобы многострочный текст был отцентрирован по вертикали
            float vAlignOffset = (maxHeaderHeight - (lines.size() * (headerFontSize + 2f))) / 2;

            // Вызываем новый метод для отрисовки с выравниванием
            drawMultilineText(lines, stream, nextX, colWidth, headerY - vAlignOffset,
                    headerFontSize + 2f, "center", font); // Используем обычный шрифт

            nextX += colWidth;
        }

        tableTopY -= maxHeaderHeight + 10;


        // Линии
        stream.setLineWidth(0.5f);
        stream.moveTo(x, tableTopY);
        stream.lineTo(x + tableWidth, tableTopY);
        stream.stroke();
        tableTopY -= 5;

        // Данные (также с центрированием для оценок)
        for (List<String> row : data) {
            tableTopY -= rowHeight;
            nextX = x;
            for (int i = 0; i < row.size(); i++) {
                float width = tableWidth * colWidths[i];
                String text = row.get(i);

                // Первую колонку (дату) оставляем слева, остальные центрируем
                String align = (i > 0) ? "center" : "left";
                float textWidth = font.getStringWidth(text) / 1000 * cellFontSize;
                float textX = (align.equals("center")) ? nextX + (width - textWidth) / 2 : nextX + cellMargin;

                addText(stream, font, cellFontSize, textX, tableTopY + (rowHeight / 2) - 5, text);
                nextX += width;
            }
            stream.moveTo(x, tableTopY);
            stream.lineTo(x + tableWidth, tableTopY);
            stream.stroke();
        }
        return tableTopY;
    }

    // Замените старый drawMultilineText на этот, который поддерживает выравнивание
    private void drawMultilineText(List<String> lines, PDPageContentStream stream, float x, float colWidth, float y, float leading, String align, PDType0Font font) throws IOException {
        final int fontSize = 9; // Размер шрифта для заголовков
        stream.setFont(font, fontSize);

        for (String line : lines) {
            float textWidth = font.getStringWidth(line) / 1000 * fontSize;
            float textX;

            if ("center".equals(align)) {
                textX = x + (colWidth - textWidth) / 2;
            } else {
                textX = x + 5f; // Левое выравнивание с отступом
            }

            stream.beginText();
            stream.newLineAtOffset(textX, y);
            stream.showText(line);
            stream.endText();
            y -= leading;
        }
    }


    /**
     * Разбивает текст на строки, чтобы он поместился в заданную ширину.
     */
    private List<String> splitTextIntoLines(String text, PDType0Font font, int fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        if (words.length == 0) {
            return lines;
        }

        StringBuilder line = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            if (font.getStringWidth(line + " " + words[i]) / 1000 * fontSize > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(words[i]);
            } else {
                line.append(" ").append(words[i]);
            }
        }
        lines.add(line.toString());
        return lines;
    }

    // Заглушки для других отчетов
    public byte[] generateProductDetailsPdf(ProductDetailsDto data, byte[] chartImage) throws IOException {
        try (PDDocument document = new PDDocument()) {
            loadFonts(document);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = 750;
            final float margin = 50;
            final float width = page.getMediaBox().getWidth() - 2 * margin;

            // --- Заголовок ---
            addText(contentStream, fontBold, 18, margin, yPosition, "Отчет: Детализация по товару");
            yPosition -= 25;
            addText(contentStream, font, 14, margin, yPosition, data.getProductName());
            yPosition -= 15;

            String reportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            addText(contentStream, font, 10, margin, yPosition, "Дата формирования: " + reportDate);
            yPosition -= 30;

            // --- KPI ---
            addText(contentStream, fontBold, 14, margin, yPosition, "Ключевые показатели");
            yPosition -= 20;
            addText(contentStream, font, 12, margin, yPosition, "Средний рейтинг: " + String.format("%.2f", data.getAverageRating()));
            addText(contentStream, font, 12, margin + 250, yPosition, "Категория: " + data.getCategoryName());
            yPosition -= 20;
            addText(contentStream, font, 12, margin, yPosition, "Количество отзывов: " + data.getReviewCount());
            addText(contentStream, font, 12, margin + 250, yPosition, "Бренд: " + data.getBrand());
            yPosition -= 30;

            // --- График ---
            addText(contentStream, fontBold, 14, margin, yPosition, "Профиль сильных/слабых сторон");
            yPosition -= 15;

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, chartImage, "pie_chart");
            float scale = width / pdImage.getWidth();
            float imgWidth = pdImage.getWidth() * scale * 0.7f; // Уменьшим, чтобы не занимал всю ширину
            float imgHeight = pdImage.getHeight() * scale * 0.7f;
            contentStream.drawImage(pdImage, margin, yPosition - imgHeight, imgWidth, imgHeight);

            contentStream.close(); // Завершаем работу с первой страницей

            // --- Таблица отзывов на новой странице ---
            if (data.getReviews() != null && !data.getReviews().isEmpty()) {
                generateReviewsTablePage(document, data);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void generateReviewsTablePage(PDDocument document, ProductDetailsDto data) throws IOException {
        PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // Заголовки таблицы
            List<String> headers = new java.util.ArrayList<>(List.of("Дата", "Рейтинг"));
            List<String> criteriaNames = data.getCriteriaProfile().stream()
                    .map(CriteriaProfileDto::getCriterionName)
                    .collect(Collectors.toList());
            headers.addAll(criteriaNames);

            // Ширина колонок (2 статические + N динамических)
            // Мы можем дать больше места дате и чуть меньше остальным
            float[] colWidths = new float[headers.size()];
            colWidths[0] = 0.15f; // Дата
            colWidths[1] = 0.1f;  // Рейтинг
            float remainingWidth = 0.75f / criteriaNames.size();
            for (int i = 0; i < criteriaNames.size(); i++) {
                colWidths[i + 2] = remainingWidth;
            }

            // Данные для таблицы
            List<List<String>> tableData = data.getReviews().stream().map(review -> {
                List<String> row = new java.util.ArrayList<>();
                row.add(review.getDateCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                row.add(String.format("%.2f", review.getIntegralRating()));

                Map<String, Integer> ratingsMap = review.getReviewRatings().stream()
                        .collect(Collectors.toMap(ReviewRatingDto::getCriterionName, ReviewRatingDto::getRating));

                for (String criteriaName : criteriaNames) {
                    row.add(ratingsMap.getOrDefault(criteriaName, 0).toString());
                }
                return row;
            }).collect(Collectors.toList());

            drawTable(document, contentStream, 50, 550, "Список отзывов по товару", headers, tableData, colWidths);
        }
    }
    public byte[] generateComparisonPdf(List<ComparisonDataDto> data) { return new byte[0]; }
}