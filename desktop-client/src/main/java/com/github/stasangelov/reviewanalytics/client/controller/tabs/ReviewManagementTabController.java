package com.github.stasangelov.reviewanalytics.client.controller.tabs;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.controller.dialog.ReviewEditDialogController;
import com.github.stasangelov.reviewanalytics.client.model.review.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiException;
import com.github.stasangelov.reviewanalytics.client.service.ReviewService;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер для вкладки "Управление отзывами".
 * Отвечает за отображение, фильтрацию и управление списком всех отзывов в системе.
 */
public class ReviewManagementTabController {

    // --- FXML Поля ---
    @FXML private TableView<ReviewDto> reviewTable;
    @FXML private TextField searchField;
    @FXML private TableColumn<ReviewDto, String> productCol;
    @FXML private TableColumn<ReviewDto, Double> ratingCol;
    @FXML private TableColumn<ReviewDto, LocalDateTime> dateCol;
    @FXML private TableColumn<ReviewDto, String> statusCol;

    // --- Зависимости и состояние ---
    private final ReviewService reviewService = new ReviewService();
    private final ObservableList<ReviewDto> allReviews = FXCollections.observableArrayList();

    //================================================================================
    // Инициализация
    //================================================================================

    /**
     * Вызывается после загрузки FXML-файла.
     * Настраивает таблицу и запускает начальную загрузку данных.
     */
    @FXML
    public void initialize() {
        setupTable();
        loadReviews();
    }

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Добавить отзыв".
     * Открывает диалоговое окно для создания нового отзыва.
     */
    @FXML
    private void handleAddReview() {
        boolean saved = showReviewEditDialog(null);
        if (saved) {
            loadReviews();
        }
    }

    /**
     * Обрабатывает нажатие на кнопку "Редактировать".
     * Открывает диалоговое окно для редактирования выбранного отзыва.
     */
    @FXML
    private void handleEditReview() {
        ReviewDto selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.showInfo("Ничего не выбрано", "Пожалуйста, выберите отзыв для редактирования.");
            return;
        }
        boolean saved = showReviewEditDialog(selected);
        if (saved) {
            loadReviews();
        }
    }

    /**
     * Обрабатывает нажатие на кнопку "Одобрить".
     * Устанавливает статус "ACTIVE" для выбранного отзыва.
     */
    @FXML
    private void handleApproveReview() {
        changeSelectedReviewStatus("ACTIVE");
    }

    /**
     * Обрабатывает нажатие на кнопку "Отклонить".
     * Устанавливает статус "REJECTED" для выбранного отзыва.
     */
    @FXML
    private void handleRejectReview() {
        changeSelectedReviewStatus("REJECTED");
    }

    //================================================================================
    // Логика работы с данными
    //================================================================================

    /**
     * Асинхронно загружает все отзывы с сервера и обновляет таблицу.
     */
    private void loadReviews() {
        new Thread(() -> {
            try {
                final List<ReviewDto> reviews = reviewService.getAllReviews();
                Platform.runLater(() -> allReviews.setAll(reviews));
            } catch (IOException e) {
                Platform.runLater(() ->
                        AlertFactory.showError("Не удалось загрузить отзывы", "Ошибка сети: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Отправляет запрос на сервер для изменения статуса выбранного отзыва.
     * Новый статус ("ACTIVE" или "REJECTED").
     */
    private void changeSelectedReviewStatus(String status) {
        ReviewDto selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.showInfo("Ничего не выбрано", "Пожалуйста, выберите отзыв для изменения статуса.");
            return;
        }

        new Thread(() -> {
            try {
                reviewService.changeStatus(selected.getId(), status);
                Platform.runLater(this::loadReviews);
            } catch (ApiException e) {
                Platform.runLater(() ->
                        AlertFactory.showError("Не удалось изменить статус", "Ошибка API (" + e.getStatusCode() + "): " + e.getMessage())
                );
            } catch (IOException e) {
                Platform.runLater(() ->
                        AlertFactory.showError("Не удалось изменить статус", "Ошибка сети: " + e.getMessage())
                );
            }
        }).start();
    }

    //================================================================================
    // Настройка и вспомогательные методы
    //================================================================================

    /**
     * Настраивает колонки таблицы, форматирование ячеек, сортировку и фильтрацию.
     */
    private void setupTable() {
        // 1. Привязываем данные к колонкам
        productCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        ratingCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIntegralRating()));
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateCreated()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        // 2. Включаем сортировку для колонок
        productCol.setSortable(true);
        ratingCol.setSortable(true);
        dateCol.setSortable(true);
        statusCol.setSortable(true);

        // 3. Форматируем ячейки
        ratingCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // 4. Настраиваем фильтрацию и сортировку
        FilteredList<ReviewDto> filteredData = new FilteredList<>(allReviews, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(review -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (review.getProductName() != null && review.getProductName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<ReviewDto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(reviewTable.comparatorProperty());

        reviewTable.setItems(sortedData);
    }

    /**
     * Открывает модальное диалоговое окно для создания/редактирования отзыва.
     */
    private boolean showReviewEditDialog(ReviewDto review) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource("review-edit-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(review == null ? "Добавление отзыва" : "Редактирование отзыва");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(reviewTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            ReviewEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setReview(review);

            dialogStage.showAndWait();
            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}