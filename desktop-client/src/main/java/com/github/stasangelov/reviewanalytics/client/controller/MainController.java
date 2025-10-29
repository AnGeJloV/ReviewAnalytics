package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.model.DashboardDto;
import com.github.stasangelov.reviewanalytics.client.model.KpiDto;
import com.github.stasangelov.reviewanalytics.client.model.TopProductDto;
import com.github.stasangelov.reviewanalytics.client.service.AnalyticsService;
import com.github.stasangelov.reviewanalytics.client.service.DictionaryService;
import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
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
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class MainController {

    @FXML
    private Menu adminMenu;

    @FXML private Label totalReviewsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private BarChart<Number, String> topProductsChart;
    @FXML private BarChart<Number, String> worstProductsChart;

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
        // Настраиваем видимость меню в зависимости от роли
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            adminMenu.setVisible(false);
        }
        setupCategoryFilter(); // Настраиваем ComboBox
        // Запускаем загрузку аналитических данных
        loadDashboardData();
    }

    /**
     * Настраивает ComboBox для категорий: загружает данные и устанавливает, как их отображать.
     */
    private void setupCategoryFilter() {
        // Настраиваем, чтобы в списке отображалось имя категории
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryDto category) {
                return category == null ? "Все категории" : category.getName();
            }
            @Override
            public CategoryDto fromString(String string) {
                return null;
            }
        });

        // Загружаем категории с сервера в фоновом потоке
        new Thread(() -> {
            try {
                final List<CategoryDto> categories = dictionaryService.getAllCategories();
                Platform.runLater(() -> categoryComboBox.getItems().addAll(categories));
            } catch (IOException e) {
                e.printStackTrace(); // TODO: Показать Alert
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

    private void updateTopProductsChart(BarChart<Number, String> chart, List<TopProductDto> data, String seriesName) {

        // Сначала полностью очищаем график от старых данных
        chart.getData().clear();

        if (data == null || data.isEmpty()) {
            return; // Если данных нет, выходим
        }

        // Получаем доступ к оси X
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();

        // 1. Находим минимальное и максимальное значения
        double min = data.stream().mapToDouble(TopProductDto::getAverageRating).min().orElse(0.0);
        double max = data.stream().mapToDouble(TopProductDto::getAverageRating).max().orElse(5.0);

        // 2. Устанавливаем границы и цену деления
        xAxis.setAutoRanging(false); // Отключаем автоматику
        xAxis.setLowerBound(Math.floor(min - 0.5));
        xAxis.setUpperBound(Math.ceil(max));
        xAxis.setTickUnit(0.5);

        // 3. Создаем и наполняем серию данных
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName(seriesName);

        Collections.reverse(data);

        for (TopProductDto product : data) {
            series.getData().add(new XYChart.Data<>(product.getAverageRating(), product.getProductName()));
        }

        // 4. И только теперь добавляем готовую серию в график
        chart.getData().add(series);

        addBarLabels(chart);
    }

    private void addBarLabels(BarChart<Number, String> chart) {
        // Этот код нужно выполнить чуть позже, чтобы JavaFX успел отрисовать столбцы.
        Platform.runLater(() -> {
            for (XYChart.Series<Number, String> series : chart.getData()) {
                for (XYChart.Data<Number, String> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        // Создаем текстовый узел (Text) с отформатированным значением
                        Text text = new Text(String.format("%.2f", data.getXValue().doubleValue()));
                        text.setFont(new Font(10));
                        text.setFill(Color.web("#333333")); // Ваш темно-серый цвет

                        // Оборачиваем узел-столбец в Group, чтобы можно было добавить текст рядом
                        Parent parent = node.getParent();
                        if (parent instanceof Group) {
                            Group group = (Group) parent;

                            // Вычисляем позицию для текста:
                            // правый край столбца + небольшой отступ
                            double x = node.getBoundsInParent().getMaxX() + 5;
                            // вертикальный центр столбца
                            double y = node.getBoundsInParent().getMinY() + node.getBoundsInParent().getHeight() / 2;

                            text.setX(x);
                            text.setY(y);

                            // Устанавливаем вертикальное выравнивание текста по центру
                            text.setTextOrigin(javafx.geometry.VPos.CENTER);

                            // Добавляем текст в ту же группу, где лежит столбец
                            group.getChildren().add(text);
                        }
                    }
                }
            }
        });
    }

    /**
     * Обновляет карточки с ключевыми показателями.
     */
    private void updateKpis(com.github.stasangelov.reviewanalytics.client.model.KpiDto kpis) {
        if (kpis != null) {
            totalReviewsLabel.setText(String.valueOf(kpis.getTotalReviews()));
            avgRatingLabel.setText(String.format("%.2f", kpis.getAverageIntegralRating()));
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
        // Очищаем данные сессии
        SessionManager.getInstance().clearSession();

        // --- ИСПРАВЛЕНИЕ ЗДЕСЬ ---
        // Получаем текущее окно (Stage) через любой Node на сцене, например, totalReviewsLabel
        Stage currentStage = (Stage) totalReviewsLabel.getScene().getWindow();

        // Вызываем новый, более надежный метод для смены сцены
        ViewSwitcher.switchScene(currentStage, "login-view.fxml");
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
    }
}