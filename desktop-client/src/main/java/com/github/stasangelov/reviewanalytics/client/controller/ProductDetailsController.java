package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.*;
import com.github.stasangelov.reviewanalytics.client.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.client.service.ComparisonService;
import javafx.application.Platform;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.scene.shape.Circle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


public class ProductDetailsController {

    // --- @FXML Поля ---
    @FXML private Label productNameLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label reviewCountLabel;
    @FXML private Label categoryLabel;
    @FXML private Label brandLabel;
    @FXML private TableView<ReviewDto> reviewsTable;
    @FXML private PieChart criteriaProfileChart;
    @FXML private FlowPane customLegendPane;
    @FXML private CheckBox compareCheckBox;
    @FXML private VBox pieChartContainer;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private Long productId;
    private ProductSummaryDto productSummary;

    /**
     * Метод для передачи ID товара из главного окна.
     */
    public void setProductId(Long productId) {
        this.productId = productId;
        loadProductDetails();
        setupPieChart();
    }

    @FXML
    public void initialize() {
        // Метод setupReviewsTable будет вызываться после загрузки данных
        criteriaProfileChart.setLegendVisible(false); // Отключаем стандартную легенду
        criteriaProfileChart.setLabelsVisible(false); // Отключаем стандартные метки на секторах
        criteriaProfileChart.setLabelLineLength(0);   // Убираем линии, ведущие к меткам
    }

    private void loadProductDetails() {
        if (productId == null) return;
        new Thread(() -> {
            try {
                final ProductDetailsDto details = analyticsService.getProductDetails(productId);
                Platform.runLater(() -> {
                    updateUi(details);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUi(ProductDetailsDto details) {
        // Сохраняем сводную информацию о товаре
        this.productSummary = new ProductSummaryDto(
                details.getProductId(),
                details.getProductName(),
                details.getCategoryName(),
                details.getBrand(),
                details.getReviewCount(),
                details.getAverageRating()
        );

        // Обновляем состояние чекбокса
        updateCompareCheckBoxState();

        productNameLabel.setText(details.getProductName());
        avgRatingLabel.setText(String.format("%.2f", details.getAverageRating()));
        reviewCountLabel.setText(String.valueOf(details.getReviewCount()));
        categoryLabel.setText(details.getCategoryName());
        brandLabel.setText(details.getBrand());

        updateCriteriaProfileChart(details.getCriteriaProfile());
        setupAndPopulateReviewsTable(details.getReviews(), details.getCriteriaProfile());
    }

    /**
     * Динамически создает колонки и заполняет таблицу отзывов.
     * @param reviews Список отзывов.
     * @param criteria Профиль критериев (для получения названий колонок).
     */
    private void setupAndPopulateReviewsTable(List<ReviewDto> reviews, List<CriteriaProfileDto> criteria) {
        reviewsTable.getColumns().clear();
        if (reviews == null || criteria == null) return;

        // 1. Создаем статические колонки
        TableColumn<ReviewDto, LocalDateTime> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateCreated()));
        dateCol.setPrefWidth(150);
        // Форматируем дату
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatter.format(item));
            }
        });

        TableColumn<ReviewDto, Double> ratingCol = new TableColumn<>("Интегр. рейтинг");
        ratingCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIntegralRating()));
        ratingCol.setPrefWidth(120);
        ratingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        reviewsTable.getColumns().addAll(dateCol, ratingCol);

        // 2. Создаем динамические колонки для каждого критерия
        for (CriteriaProfileDto criterion : criteria) {
            TableColumn<ReviewDto, Integer> criterionCol = new TableColumn<>(criterion.getCriterionName());
            criterionCol.setPrefWidth(120);

            criterionCol.setCellValueFactory(cellData -> {
                // Для каждой строки (отзыва) ищем оценку по текущему критерию
                ReviewDto review = cellData.getValue();
                Integer ratingValue = review.getReviewRatings().stream()
                        .filter(r -> r.getCriterionName().equals(criterion.getCriterionName()))
                        .map(ReviewRatingDto::getRating)
                        .findFirst()
                        .orElse(null); // Если оценки нет, ячейка будет пустой
                return new SimpleObjectProperty<>(ratingValue);
            });

            reviewsTable.getColumns().add(criterionCol);
        }

        // 3. Заполняем таблицу данными
        reviewsTable.setItems(FXCollections.observableArrayList(reviews));
    }

    /**
     * Настраивает PieChart, добавляя "слушателя" для установки подсказок и стилей.
     */
    private void setupPieChart() {
        // Этот "слушатель" будет срабатывать каждый раз, когда в график добавляются новые данные
        criteriaProfileChart.dataProperty().addListener((obs, oldData, newData) -> {
            if (newData == null) return;

            // Считаем общую сумму для расчета процентов
            double totalValue = newData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

            // Проходим по новым данным и настраиваем каждый сектор
            newData.forEach(data -> {
                Node node = data.getNode();
                if (node != null) {
                    double percentage = (data.getPieValue() / totalValue) * 100;
                    String tooltipText = String.format("%s: %.2f (%.1f%%)", data.getName(), data.getPieValue(), percentage);
                    Tooltip.install(node, new Tooltip(tooltipText));
                }
            });
        });
    }

    /**
     * Обновляет PieChart и создает кастомную легенду.
     */
    private void updateCriteriaProfileChart(List<CriteriaProfileDto> data) {
        if (data == null || data.isEmpty()) {
            criteriaProfileChart.setVisible(false);
            return;
        }
        criteriaProfileChart.setVisible(true);

        data.sort((p1, p2) -> p2.getAverageRating().compareTo(p1.getAverageRating()));

        double totalValue = data.stream().mapToDouble(CriteriaProfileDto::getAverageRating).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (CriteriaProfileDto profile : data) {
            pieChartData.add(new PieChart.Data(profile.getCriterionName(), profile.getAverageRating()));
        }

        criteriaProfileChart.setData(pieChartData);
        criteriaProfileChart.setStartAngle(90);

        // Применяем подсказки, стили и создаем кастомную легенду
        Platform.runLater(() -> applyTooltipsAndStyles(totalValue));
    }

    /**
     * Проходит по отрисованным секторам, применяет стили,
     * устанавливает Tooltip и строит кастомную легенду.
     */
    private void applyTooltipsAndStyles(double totalValue) {
        // Очищаем старую легенду
        customLegendPane.getChildren().clear();

        int colorIndex = 0;
        String[] colors = {"#333333", "#4D4D4D", "#666666", "#808080", "#999999", "#B3B3B3", "#CCCCCC", "#E0E0E0"};

        for (PieChart.Data d : criteriaProfileChart.getData()) {
            Node node = d.getNode();
            if (node != null) {
                // 1. Применяем стиль цвета
                String colorHex = colors[colorIndex % colors.length];
                node.setStyle("-fx-pie-color: " + colorHex + "; -fx-border-color: white; -fx-border-width: 2px;");

                // 2. Устанавливаем всплывающую подсказку
                double percentage = (d.getPieValue() / totalValue) * 100;
                String tooltipText = String.format("%s: %.2f (%.1f%%)", d.getName(), d.getPieValue(), percentage);
                Tooltip tooltip = new Tooltip(tooltipText);

                // Устанавливаем обработчики для показа/скрытия подсказки
                node.setOnMouseEntered(event -> {
                    tooltip.show(node.getScene().getWindow(), event.getScreenX() + 10, event.getScreenY() + 10);
                });
                node.setOnMouseExited(event -> {
                    tooltip.hide();
                });

                // 3. Создаем элемент для кастомной легенды
                HBox legendEntry = new HBox(5);
                legendEntry.setAlignment(Pos.CENTER_LEFT);

                Circle colorCircle = new Circle(5, Color.web(colorHex));
                Label legendLabel = new Label(d.getName());

                legendEntry.getChildren().addAll(colorCircle, legendLabel);
                customLegendPane.getChildren().add(legendEntry);

                colorIndex++;
            }
        }
    }

    /**
     * НОВЫЙ МЕТОД: Обработчик для CheckBox.
     */
    @FXML
    private void handleToggleCompare() {
        if (this.productSummary == null) return;

        if (compareCheckBox.isSelected()) {
            boolean success = ComparisonService.getInstance().addProduct(this.productSummary);
            if (!success) {
                compareCheckBox.setSelected(false);
            }
        } else {
            ComparisonService.getInstance().removeProduct(this.productSummary);
        }
    }

    /**
     * Обновляет состояние CheckBox в зависимости от того, есть ли товар в сравнении.
     */
    private void updateCompareCheckBoxState() {
        compareCheckBox.setSelected(ComparisonService.getInstance().contains(this.productId));
    }

    @FXML
    void exportDetailsToPdf(ActionEvent event) {
        if (this.productId == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет по товару");
        fileChooser.setInitialFileName("product_report_" + this.productId + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        File file = fileChooser.showSaveDialog(productNameLabel.getScene().getWindow());

        if (file != null) {
            try {
                // Делаем снимок PieChart
                byte[] chartSnapshot = snapshotNode(pieChartContainer);

                new Thread(() -> {
                    try {
                        byte[] pdfData = analyticsService.getProductDetailsPdf(this.productId, chartSnapshot);
                        Files.write(file.toPath(), pdfData);

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Отчет успешно сохранен: " + file.getAbsolutePath());
                            alert.setTitle("Успех");
                            alert.setHeaderText(null);
                            alert.showAndWait();
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                            alert.setTitle("Ошибка сохранения");
                            alert.setHeaderText("Не удалось сформировать или сохранить отчет.");
                            alert.showAndWait();
                        });
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                alert.setTitle("Ошибка снимка");
                alert.setHeaderText("Не удалось сделать снимок графика.");
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    /**
     * Вспомогательный метод для создания снимка любого JavaFX узла (Node) и преобразования его в byte[].
     */
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
