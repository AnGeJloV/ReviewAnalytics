package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.model.analytics.dashboard.*;
import com.github.stasangelov.reviewanalytics.client.model.analytics.product.ProductSummaryDto;
import com.github.stasangelov.reviewanalytics.client.model.dictionary.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.client.service.ComparisonService;
import com.github.stasangelov.reviewanalytics.client.service.DictionaryService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для главной информационной панели (дашборда).
 * Отвечает за загрузку и отображение всей аналитики, обработку фильтров,
 * навигацию и экспорт отчета.
 */
public class DashboardController {

    // --- FXML Поля ---
    @FXML private Label totalReviewsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private BarChart<Number, String> topProductsChart;
    @FXML private BarChart<Number, String> worstProductsChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private LineChart<String, Number> dynamicsChart;
    @FXML private Label categoryChartTitle;
    @FXML private StackedBarChart<String, Number> distributionChart;
    @FXML private ScrollPane distributionChartScrollPane;
    @FXML private TableColumn<ProductSummaryDto, Boolean> selectCol;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<CategoryDto> categoryComboBox;
    @FXML private TextField productSearchField;
    @FXML private TableView<ProductSummaryDto> productsSummaryTable;
    @FXML private TableColumn<ProductSummaryDto, String> productNameCol;
    @FXML private TableColumn<ProductSummaryDto, String> categoryCol;
    @FXML private TableColumn<ProductSummaryDto, String> brandCol;
    @FXML private TableColumn<ProductSummaryDto, Long> reviewCountCol;
    @FXML private TableColumn<ProductSummaryDto, Double> avgRatingCol;

    // --- Зависимости и состояние ---
    private final ObservableList<ProductSummaryDto> allProductsSummary = FXCollections.observableArrayList();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final DictionaryService dictionaryService = new DictionaryService();

    //================================================================================
    // Инициализация
    //================================================================================

    /**
     * Вызывается после загрузки FXML-файла.
     * Настраивает все компоненты UI и запускает начальную загрузку данных.
     */
    @FXML
    public void initialize() {
        setupCategoryFilter();
        loadDashboardData();
        setupProductsSummaryTable();
    }

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обработчик нажатия кнопки "Применить".
     * Просто вызывает основной метод загрузки данных.
     */
    @FXML
    private void applyFilters() {
        loadDashboardData();
    }

    /**
     * Обработчик нажатия кнопки "Сбросить".
     * Очищает все поля фильтров и перезагружает данные.
     */
    @FXML
    private void resetFilters() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        categoryComboBox.getSelectionModel().clearSelection();
        loadDashboardData();
    }

    /**
     * Обрабатывает экспорт текущего вида в PDF-отчет.
     * Собирает фильтры, делает снимки графиков и отправляет на сервер.
     */
    @FXML
    void exportDashboardToPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.setInitialFileName("dashboard_report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        File file = fileChooser.showSaveDialog(totalReviewsLabel.getScene().getWindow());

        if (file != null) {
            // Собираем фильтры
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            CategoryDto selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            Long categoryId = (selectedCategory != null) ? selectedCategory.getId() : null;

            // Делаем снимки графиков
            try {
                Map<String, byte[]> chartImages = new HashMap<>();
                chartImages.put("categoryChart.png", snapshotNode(categoryChart));
                chartImages.put("dynamicsChart.png", snapshotNode(dynamicsChart));
                chartImages.put("distributionChart.png", snapshotNode(distributionChart));

                new Thread(() -> {
                    try {
                        byte[] pdfData = analyticsService.getDashboardPdf(startDate, endDate, categoryId, chartImages);
                        Files.write(file.toPath(), pdfData);

                        Platform.runLater(() -> {
                            AlertFactory.showInfo("Отчет успешно сохранен", "Файл сохранен по пути: " + file.getAbsolutePath());
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            AlertFactory.showError("Не удалось сохранить отчет", e.getMessage());
                        });
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                AlertFactory.showError("Не удалось сделать снимок графика", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //================================================================================
    // Загрузка и обновление данных
    //================================================================================

    /**
     * Асинхронно загружает все данные для дашборда с сервера.
     * Собирает текущие значения фильтров и передает их в сервис.
     * После получения данных вызывает методы для обновления каждого элемента UI.
     */
    private void loadDashboardData() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        CategoryDto selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        Long categoryId = (selectedCategory != null) ? selectedCategory.getId() : null;

        new Thread(() -> {
            try {
                final DashboardDto dashboardData = analyticsService.getDashboardData(startDate, endDate, categoryId);
                final List<ProductSummaryDto> productsSummary = analyticsService.getProductsSummary(startDate, endDate, categoryId);
                Platform.runLater(() -> {
                    updateKpis(dashboardData.getKpis());
                    updateTopProductsChart(topProductsChart, dashboardData.getTopRatedProducts(), "Лучшие");
                    updateTopProductsChart(worstProductsChart, dashboardData.getWorstRatedProducts(), "Худшие");
                    if (categoryId != null) {
                        // Если был фильтр по категории, показываем бренды
                        updateBrandChart(dashboardData.getBrandRatings());
                    } else {
                        // Иначе показываем категории
                        updateCategoryChart(dashboardData.getCategoryRatings());
                    }
                    updateDynamicsChart(dashboardData.getRatingDynamics());
                    updateDistributionChart(dashboardData.getRatingDistribution());
                    allProductsSummary.setAll(productsSummary);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    AlertFactory.showError("Не удалось загрузить данные для дашборда", e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Обновляет карточки с ключевыми показателями (KPI).
     */
    private void updateKpis(KpiDto kpis) {
        if (kpis != null) {
            totalReviewsLabel.setText(String.valueOf(kpis.getTotalReviews()));
            avgRatingLabel.setText(String.format("%.2f", kpis.getAverageIntegralRating()));
        } else {
            totalReviewsLabel.setText("0");
            avgRatingLabel.setText("0.00");
        }
    }

    /**
     * Обновляет один из BarChart'ов (топ лучших/худших товаров).
     */
    private void updateTopProductsChart(BarChart<Number, String> chart, List<TopProductDto> data, String seriesName) {
        chart.getData().clear();

        if (data == null || data.isEmpty()) {
            return;
        }

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        double min = data.stream().mapToDouble(TopProductDto::getAverageRating).min().orElse(0.0);
        double max = data.stream().mapToDouble(TopProductDto::getAverageRating).max().orElse(5.0);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(Math.floor(min - 0.5));
        xAxis.setUpperBound(Math.ceil(max));
        xAxis.setTickUnit(0.5);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName(seriesName);

        Collections.reverse(data);

        for (TopProductDto product : data) {
            series.getData().add(new XYChart.Data<>(product.getAverageRating(), product.getProductName()));
        }

        chart.getData().add(series);
    }

    /**
     * Обновляет BarChart с данными о среднем рейтинге по категориям.
     */
    private void updateCategoryChart(List<CategoryRatingDto> data) {
        categoryChartTitle.setText("Средний рейтинг по категориям");
        categoryChart.getData().clear();
        if (data == null || data.isEmpty()) return;

        NumberAxis yAxis = (NumberAxis) categoryChart.getYAxis();
        double min = data.stream().mapToDouble(CategoryRatingDto::getAverageRating).min().orElse(0.0);
        double max = data.stream().mapToDouble(CategoryRatingDto::getAverageRating).max().orElse(5.0);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Math.floor(min - 0.5));
        yAxis.setUpperBound(Math.ceil(max));
        yAxis.setTickUnit(0.5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (CategoryRatingDto categoryRating : data) {
            series.getData().add(new XYChart.Data<>(categoryRating.getCategoryName(), categoryRating.getAverageRating()));
        }
        categoryChart.getData().add(series);
    }

    /**
     * Обновляет BarChart с данными о среднем рейтинге по брендам.
     */
    private void updateBrandChart(List<BrandRatingDto> data) {
        categoryChartTitle.setText("Средний рейтинг по брендам");
        categoryChart.getData().clear();
        if (data == null || data.isEmpty()) return;

        // Настраиваем ось Y
        NumberAxis yAxis = (NumberAxis) categoryChart.getYAxis();
        double min = data.stream().mapToDouble(BrandRatingDto::getAverageRating).min().orElse(0.0);
        double max = data.stream().mapToDouble(BrandRatingDto::getAverageRating).max().orElse(5.0);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Math.floor(min - 0.5));
        yAxis.setUpperBound(Math.ceil(max));
        yAxis.setTickUnit(0.5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (BrandRatingDto item : data) {
            series.getData().add(new XYChart.Data<>(item.getBrandName(), item.getAverageRating()));
        }
        categoryChart.getData().add(series);
    }

    /**
     * Обновляет LineChart, отображающий динамику среднего рейтинга.
     */
    private void updateDynamicsChart(List<RatingDynamicDto> data) {
        dynamicsChart.getData().clear();
        if (data == null || data.isEmpty()) return;

        NumberAxis yAxis = (NumberAxis) dynamicsChart.getYAxis();
        double min = data.stream().mapToDouble(RatingDynamicDto::getAverageRating).min().orElse(0.0);
        double max = data.stream().mapToDouble(RatingDynamicDto::getAverageRating).max().orElse(5.0);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Math.floor(min - 0.5));
        yAxis.setUpperBound(Math.ceil(max));
        yAxis.setTickUnit(0.5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        DateTimeFormatter formatter;
        if (data.size() > 31) {
            formatter = DateTimeFormatter.ofPattern("dd.MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        }

        for (RatingDynamicDto dynamic : data) {
            series.getData().add(new XYChart.Data<>(dynamic.getDate().format(formatter), dynamic.getAverageRating()));
        }
        dynamicsChart.getData().add(series);
    }

    /**
     * Обновляет StackedBarChart, отображающий распределение оценок по критериям.
     */
    private void updateDistributionChart(List<RatingDistributionDto> data) {
        distributionChart.getData().clear();

        double scrollPaneWidth = distributionChartScrollPane.getWidth();

        if (scrollPaneWidth <= 0 && distributionChartScrollPane.getScene() != null) {
            scrollPaneWidth = distributionChartScrollPane.getScene().getWidth() / 2 - 40;
        }

        if (data == null || data.isEmpty()) {
            distributionChart.setPrefWidth(0);
            return;
        }

        int numberOfBars = data.size();
        double barWidth = 75;
        double categoryGap = 25;
        double calculatedWidth = (numberOfBars * (barWidth + categoryGap)) + 100;

        distributionChart.setMinWidth(Math.max(calculatedWidth, scrollPaneWidth));

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Оценка 1");
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Оценка 2");
        XYChart.Series<String, Number> series3 = new XYChart.Series<>();
        series3.setName("Оценка 3");
        XYChart.Series<String, Number> series4 = new XYChart.Series<>();
        series4.setName("Оценка 4");
        XYChart.Series<String, Number> series5 = new XYChart.Series<>();
        series5.setName("Оценка 5");

        // Проходим по данным и для каждого критерия добавляем количество оценок в соответствующую серию
        for (RatingDistributionDto distribution : data) {
            series1.getData().add(new XYChart.Data<>(distribution.getCriterionName(), distribution.getRating1Count()));
            series2.getData().add(new XYChart.Data<>(distribution.getCriterionName(), distribution.getRating2Count()));
            series3.getData().add(new XYChart.Data<>(distribution.getCriterionName(), distribution.getRating3Count()));
            series4.getData().add(new XYChart.Data<>(distribution.getCriterionName(), distribution.getRating4Count()));
            series5.getData().add(new XYChart.Data<>(distribution.getCriterionName(), distribution.getRating5Count()));
        }

        distributionChart.getData().addAll(series1, series2, series3, series4, series5);
    }

    //================================================================================
    // Настройка UI компонентов
    //================================================================================

    /**
     * Настраивает выпадающий список (ComboBox) для фильтрации по категориям.
     * Загружает список категорий с сервера.
     */
    private void setupCategoryFilter() {
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryDto category) {
                return category == null ? "Все категории" : category.getName();
            }
            @Override
            public CategoryDto fromString(String string) { return null; }
        });

        new Thread(() -> {
            try {
                final List<CategoryDto> categories = dictionaryService.getAllCategories();
                Platform.runLater(() -> categoryComboBox.getItems().addAll(categories));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Настраивает главную таблицу (TableView) со сводкой по всем товарам.
     * Включает настройку колонок, форматирования, фильтрации, сортировки
     * и обработчика двойного клика для перехода к детализации.
     */
    private void setupProductsSummaryTable() {
        // 1. Привязываем колонки к полям DTO
        productNameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        categoryCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoryName()));
        brandCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
        reviewCountCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getReviewCount()));
        avgRatingCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAverageRating()));
        selectCol.setCellValueFactory(cellData -> {
            ProductSummaryDto summary = cellData.getValue();
            boolean isSelected = ComparisonService.getInstance().contains(summary.getProductId());
            return new SimpleObjectProperty<>(isSelected);
        });
        selectCol.setCellFactory(param -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                // Центрируем чекбокс в ячейке
                setAlignment(Pos.CENTER);

                checkBox.setOnAction(event -> {
                    ProductSummaryDto summary = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        boolean success = ComparisonService.getInstance().addProduct(summary);
                        if (!success) {
                            checkBox.setSelected(false);
                        }
                    } else {
                        ComparisonService.getInstance().removeProduct(summary);
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });

        productNameCol.setSortable(true);
        categoryCol.setSortable(true);
        brandCol.setSortable(true);
        reviewCountCol.setSortable(true);
        avgRatingCol.setSortable(true);

        // 2. Форматируем колонку с рейтингом
        avgRatingCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // 3. Настраиваем фильтрацию (поиск)
        FilteredList<ProductSummaryDto> filteredData = new FilteredList<>(allProductsSummary, p -> true);
        productSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(summary -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return summary.getProductName().toLowerCase().contains(lowerCaseFilter)
                        || summary.getBrand().toLowerCase().contains(lowerCaseFilter);
            });
        });
        productsSummaryTable.setItems(filteredData);

        // 4. Привязываем отфильтрованные и сортируемые данные к таблице
        SortedList<ProductSummaryDto> sortedData = new SortedList<>(filteredData);
        // Привязываем компаратор (логику сортировки) таблицы к SortedList
        sortedData.comparatorProperty().bind(productsSummaryTable.comparatorProperty());

        // Устанавливаем отсортированные данные в таблицу
        productsSummaryTable.setItems(sortedData);

        // 5. Настраиваем переход к детализации по двойному клику
        productsSummaryTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                ProductSummaryDto selected = productsSummaryTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openProductDetailsWindow(selected.getProductId());
                }
            }
        });
    }

    //================================================================================
    // Навигация и вспомогательные методы
    //================================================================================

    /**
     * Открывает новое модальное окно с детализированной информацией по товару.
     */
    private void openProductDetailsWindow(Long productId) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource("product-details-view.fxml"));
            ScrollPane page = loader.load();

            ProductDetailsController controller = loader.getController();

            controller.setProductId(productId);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Детализированная аналитика по товару");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productsSummaryTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page, 1400, 800));

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает снимок любого JavaFX узла (Node) и преобразует его в массив байт.
     */
    private byte[] snapshotNode(Node node) throws IOException {
        WritableImage image = node.snapshot(new SnapshotParameters(), null);

        // Устанавливаем белый фон вместо прозрачного для лучшего вида в PDF
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