package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.ComparisonDataDto;
import com.github.stasangelov.reviewanalytics.client.model.CriteriaProfileDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductSummaryDto;
import com.github.stasangelov.reviewanalytics.client.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.client.service.ComparisonService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparisonController {

    @FXML private AreaChart<String, Number> comparisonChart;
    @FXML private TableView<Map<String, Object>> comparisonTable;
    @FXML private FlowPane customLegendPane;
    @FXML private VBox areaChartContainer;
    @FXML private VBox comparisonTableContainer;

    @FXML private VBox placeholderPane;
    @FXML private Label placeholderLabel;
    @FXML private VBox contentPane;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        // Добавляем "слушателя", который будет реагировать на изменения в списке сравнения
        ComparisonService.getInstance().getItemsToCompare().addListener((ListChangeListener<ProductSummaryDto>) c -> {
            loadComparisonData();
        });

        // Загружаем данные при первой загрузке вида
        loadComparisonData();
    }

    public void exportViewToPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет по сравнению");
        fileChooser.setInitialFileName("comparison_report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        File file = fileChooser.showSaveDialog(areaChartContainer.getScene().getWindow());

        if (file != null) {
            List<Long> productIds = ComparisonService.getInstance().getItemsToCompare().stream()
                    .map(ProductSummaryDto::getProductId)
                    .collect(Collectors.toList());
            if (productIds.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Нет товаров для сравнения.").showAndWait();
                return;
            }
            try {
                Map<String, byte[]> images = new HashMap<>();
                images.put("comparisonChart.png", snapshotNode(areaChartContainer));
                images.put("comparisonTable.png", snapshotNode(comparisonTableContainer));

                new Thread(() -> {
                    try {
                        byte[] pdfData = analyticsService.getComparisonPdf(productIds, images);
                        Files.write(file.toPath(), pdfData);
                        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Отчет успешно сохранен: " + file.getAbsolutePath()).showAndWait());
                    } catch (IOException e) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Не удалось сформировать или сохранить отчет: " + e.getMessage()).showAndWait());
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Не удалось сделать снимок: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void loadComparisonData() {
        List<Long> productIds = ComparisonService.getInstance().getItemsToCompare().stream()
                .map(ProductSummaryDto::getProductId)
                .collect(Collectors.toList());

        int count = productIds.size();

        if (count < 2) {
            // Показываем заглушку
            placeholderPane.setVisible(true);
            placeholderPane.setManaged(true);
            // Скрываем контент и убираем его из расчета размеров
            contentPane.setVisible(false);
            contentPane.setManaged(false);

            if (count == 0) {
                placeholderLabel.setText("Выберите 2 или более товара на главном экране для их сравнения.");
            } else {
                placeholderLabel.setText("Выберите еще хотя бы один товар для начала сравнения.");
            }

            // Очищаем старые данные
            comparisonChart.getData().clear();
            comparisonTable.getColumns().clear();
            comparisonTable.getItems().clear();
            return;
        }

        // Показываем контент и включаем его в расчет размеров
        placeholderPane.setVisible(false);
        placeholderPane.setManaged(false);
        contentPane.setVisible(true);
        contentPane.setManaged(true);

        new Thread(() -> {
            try {
                final List<ComparisonDataDto> comparisonData = analyticsService.getComparisonData(productIds);
                Platform.runLater(() -> {
                    updateChart(comparisonData);
                    updateTable(comparisonData);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void updateChart(List<ComparisonDataDto> data) {
        comparisonChart.getData().clear();
        if (data == null || data.isEmpty()) return;

        // Собираем все уникальные и отсортированные критерии для оси X
        List<String> criteriaOrder = data.stream()
                .flatMap(d -> d.getCriteriaProfile().stream().map(CriteriaProfileDto::getCriterionName))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();
        for (ComparisonDataDto productData : data) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(productData.getProductName());

            Map<String, Double> ratingsMap = productData.getCriteriaProfile().stream()
                    .collect(Collectors.toMap(CriteriaProfileDto::getCriterionName, CriteriaProfileDto::getAverageRating));

            for (String criterionName : criteriaOrder) {
                series.getData().add(new XYChart.Data<>(criterionName, ratingsMap.getOrDefault(criterionName, 0.0)));
            }
            seriesList.add(series);
        }

        comparisonChart.setAnimated(false);
        comparisonChart.getData().addAll(seriesList);

        Platform.runLater(() -> {
            applyChartStylesAndLegend(seriesList);
            adjustYAxis(data); // <-- НОВЫЙ ВЫЗОВ
        });
    }

    /**
     * НОВЫЙ МЕТОД: Динамически настраивает ось Y.
     */
    private void adjustYAxis(List<ComparisonDataDto> data) {
        NumberAxis yAxis = (NumberAxis) comparisonChart.getYAxis();

        double min = data.stream()
                .flatMap(d -> d.getCriteriaProfile().stream())
                .mapToDouble(CriteriaProfileDto::getAverageRating)
                .min().orElse(0.0);
        double max = data.stream()
                .flatMap(d -> d.getCriteriaProfile().stream())
                .mapToDouble(CriteriaProfileDto::getAverageRating)
                .max().orElse(5.0);

        yAxis.setAutoRanging(false);
        // Устанавливаем нижнюю границу чуть ниже минимума, но не меньше 0
        yAxis.setLowerBound(Math.max(0, Math.floor(min - 0.5)));
        // Устанавливаем верхнюю границу чуть выше максимума, но не больше 5
        yAxis.setUpperBound(Math.min(5.0, Math.ceil(max)));
        yAxis.setTickUnit(0.5);
    }

    /**
     * Программно применяет стили к AreaChart и строит кастомную легенду.
     */
    private void applyChartStylesAndLegend(List<XYChart.Series<String, Number>> seriesList) {
        customLegendPane.getChildren().clear();
        String[] strokeColors = {"#333333", "#666666", "#999999", "#CCCCCC"};
        String[] strokeDashArrays = {"", "5, 5", "10, 5", "2, 5"};

        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series<String, Number> series = seriesList.get(i);
            Node seriesNode = series.getNode();

            String stroke = strokeColors[i % strokeColors.length];
            String dash = strokeDashArrays[i % strokeDashArrays.length];

            if (seriesNode != null) {
                StringBuilder styleBuilder = new StringBuilder();
                styleBuilder.append(String.format("-fx-fill: transparent; -fx-stroke: %s; ", stroke));

                // Добавляем стиль пунктира, только если он не пустой
                if (dash != null && !dash.isEmpty()) {
                    styleBuilder.append(String.format("-fx-stroke-dash-array: %s; ", dash));
                }

                seriesNode.setStyle(styleBuilder.toString());

                // Стили для точек (код без изменений)
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle(String.format("-fx-background-color: %s;", stroke));
                    }
                }
            }

            HBox legendEntry = new HBox(5);
            legendEntry.setAlignment(Pos.CENTER_LEFT);
            Circle colorCircle = new Circle(5, Color.web(stroke)); // Кружок с цветом линии
            Label legendLabel = new Label(series.getName());
            legendEntry.getChildren().addAll(colorCircle, legendLabel);
            customLegendPane.getChildren().add(legendEntry);
        }
    }

    private void updateTable(List<ComparisonDataDto> data) {
        comparisonTable.getColumns().clear();
        comparisonTable.getItems().clear();

        if (data == null || data.isEmpty()) return;

        // 1. Создаем первую колонку для названий критериев
        TableColumn<Map<String, Object>, String> criteriaCol = new TableColumn<>("Критерий");
        criteriaCol.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("criterionName")));
        criteriaCol.setPrefWidth(200); // Задаем ширину
        comparisonTable.getColumns().add(criteriaCol);

        // 2. Создаем по одной колонке на каждый товар
        for (ComparisonDataDto productData : data) {
            TableColumn<Map<String, Object>, Double> productCol = new TableColumn<>(productData.getProductName());
            productCol.setCellValueFactory(cellData -> {
                Double value = (Double) cellData.getValue().get(productData.getProductName());
                return new SimpleObjectProperty<>(value);
            });
            productCol.setPrefWidth(150); // Задаем ширину

            // Добавляем форматирование и подсветку
            productCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    // Сбрасываем стиль перед новой отрисовкой
                    setStyle("");

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));

                        // Логика подсветки
                        if (getIndex() < getTableView().getItems().size()) {
                            Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                            double maxInRow = rowData.values().stream()
                                    .filter(v -> v instanceof Double)
                                    .mapToDouble(v -> (Double) v)
                                    .max().orElse(0.0);

                            if (item == maxInRow && maxInRow > 0) {
                                setStyle("-fx-background-color: #666666; -fx-text-fill: white;");
                            }
                        }
                    }
                }
            });
            comparisonTable.getColumns().add(productCol);
        }

        // 3. Преобразуем данные в формат "строка-в-карте"
        List<String> allCriteria = data.stream()
                .flatMap(d -> d.getCriteriaProfile().stream().map(CriteriaProfileDto::getCriterionName))
                .distinct().sorted().collect(Collectors.toList());

        ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();
        for (String criterionName : allCriteria) {
            Map<String, Object> row = new HashMap<>();
            row.put("criterionName", criterionName);
            for (ComparisonDataDto productData : data) {
                productData.getCriteriaProfile().stream()
                        .filter(p -> p.getCriterionName().equals(criterionName))
                        .findFirst()
                        .ifPresent(p -> row.put(productData.getProductName(), p.getAverageRating()));
            }
            tableData.add(row);
        }
        comparisonTable.setItems(tableData);
    }

    @FXML
    void exportComparisonToPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет по сравнению");
        fileChooser.setInitialFileName("comparison_report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        File file = fileChooser.showSaveDialog(areaChartContainer.getScene().getWindow());

        if (file != null) {
            List<Long> productIds = ComparisonService.getInstance().getItemsToCompare().stream()
                    .map(ProductSummaryDto::getProductId)
                    .collect(Collectors.toList());

            if (productIds.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Нет товаров для сравнения.").showAndWait();
                return;
            }

            try {
                // --- НАЧАЛО ИЗМЕНЕНИЙ ---

                // Создаем Map для хранения всех снимков
                Map<String, byte[]> images = new HashMap<>();
                // Делаем снимок контейнера с графиком и легендой
                images.put("comparisonChart.png", snapshotNode(areaChartContainer));
                // Делаем снимок таблицы
                images.put("comparisonTable.png", snapshotNode(comparisonTable));

                new Thread(() -> {
                    try {
                        // Передаем в сервис всю Map с изображениями
                        byte[] pdfData = analyticsService.getComparisonPdf(productIds, images);
                        Files.write(file.toPath(), pdfData);

                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.INFORMATION, "Отчет успешно сохранен: " + file.getAbsolutePath()).showAndWait();
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.ERROR, "Не удалось сформировать или сохранить отчет: " + e.getMessage()).showAndWait();
                        });
                        e.printStackTrace();
                    }
                }).start();

                // --- КОНЕЦ ИЗМЕНЕНИЙ ---

            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Не удалось сделать снимок: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }

    private byte[] snapshotNode(Node node) throws IOException {
        WritableImage image = node.snapshot(new SnapshotParameters(), null);

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        BufferedImage imageWithBackground = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageWithBackground.getGraphics().setColor(java.awt.Color.WHITE);
        imageWithBackground.getGraphics().fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        imageWithBackground.getGraphics().drawImage(bufferedImage, 0, 0, null);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(imageWithBackground, "png", outputStream);
            return outputStream.toByteArray();
        }
    }
}
