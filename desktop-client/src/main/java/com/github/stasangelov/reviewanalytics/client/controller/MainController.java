package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.*;
import com.github.stasangelov.reviewanalytics.client.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.client.service.DictionaryService;
import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.chart.LineChart;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainController {

    // --- Элементы меню ---
    @FXML private Menu adminMenu;

    // --- Элементы дашборда ---
    @FXML private Label totalReviewsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private BarChart<Number, String> topProductsChart;
    @FXML private BarChart<Number, String> worstProductsChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private LineChart<String, Number> dynamicsChart;
    @FXML private Label categoryChartTitle;

    // --- Элементы фильтров ---
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<CategoryDto> categoryComboBox;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final DictionaryService dictionaryService = new DictionaryService();

    /**
     * Метод, который вызывается JavaFX после загрузки FXML.
     * Здесь мы настраиваем UI и запускаем загрузку данных.
     */
    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            adminMenu.setVisible(false);
        }
        setupCategoryFilter();
        loadDashboardData();
    }

    /**
     * Настраивает ComboBox для категорий: загружает данные и устанавливает, как их отображать.
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
     * Асинхронно загружает данные с сервера и обновляет UI.
     */
    private void loadDashboardData() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        CategoryDto selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        Long categoryId = (selectedCategory != null) ? selectedCategory.getId() : null;

        new Thread(() -> {
            try {
                final DashboardDto dashboardData = analyticsService.getDashboardData(startDate, endDate, categoryId);
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
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка загрузки");
                    alert.setHeaderText("Не удалось загрузить данные для дашборда.");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Обновляет график сравнения категорий с динамической осью Y.
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
     * НОВЫЙ МЕТОД: Обновляет тот же самый график, но данными по брендам.
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
     * Обновляет график динамики рейтинга с динамической осью Y.
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
        if (data.size() > 31) { // Если точек много, скорее всего это дни за несколько месяцев
            formatter = DateTimeFormatter.ofPattern("dd.MM");
        } else { // Если точек мало, это недели или месяцы, покажем месяц
            formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        }


        for (RatingDynamicDto dynamic : data) {
            series.getData().add(new XYChart.Data<>(dynamic.getDate().format(formatter), dynamic.getAverageRating()));
        }
        dynamicsChart.getData().add(series);
    }

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
     * Обновляет карточки с ключевыми показателями.
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

    @FXML
    void onGoToAdminPanel(ActionEvent event) {
        try {
            ViewSwitcher.showModalWindow("admin-view.fxml", "Администрирование");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть панель администрирования.");
            alert.showAndWait();
        }
    }

    /**
     * Обработчик для выхода из системы.
     */
    @FXML
    void onLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        Stage currentStage = (Stage) totalReviewsLabel.getScene().getWindow();
        ViewSwitcher.switchScene(currentStage, "login-view.fxml");
    }
}