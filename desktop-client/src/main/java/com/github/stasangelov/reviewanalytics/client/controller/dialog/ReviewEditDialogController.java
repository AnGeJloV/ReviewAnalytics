package com.github.stasangelov.reviewanalytics.client.controller.dialog;

import com.github.stasangelov.reviewanalytics.client.model.dictionary.CriterionDto;
import com.github.stasangelov.reviewanalytics.client.model.dictionary.ProductDto;
import com.github.stasangelov.reviewanalytics.client.model.review.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.model.review.ReviewRatingDto;
import com.github.stasangelov.reviewanalytics.client.service.DictionaryService;
import com.github.stasangelov.reviewanalytics.client.service.ReviewService;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для диалогового окна создания и редактирования отзыва.
 * Работает в двух режимах:
 * - Режим создания: review == null.
 * - Режим редактирования: review != null.
 */
public class ReviewEditDialogController {

    // --- FXML Поля ---
    @FXML private Label titleLabel;
    @FXML private ComboBox<ProductDto> productComboBox;
    @FXML private DatePicker datePicker;
    @FXML private GridPane criteriaGrid;
    @FXML private Label errorLabel;

    // --- Зависимости и состояние ---
    @Setter private Stage dialogStage;
    @Getter private boolean saveClicked = false;

    private final DictionaryService dictionaryService = new DictionaryService();
    private final ReviewService reviewService = new ReviewService();

    private ReviewDto review;
    private List<CriterionDto> criteriaList;

    //================================================================================
    // Инициализация и настройка
    //================================================================================

    /**
     * Вызывается после загрузки FXML-файла.
     * Инициализирует начальное состояние окна.
     */
    @FXML
    private void initialize() {
        datePicker.setValue(java.time.LocalDate.now());
        configureComboBox();
        loadProducts();

        // Добавляем слушателя на выбор товара. Когда товар выбран,
        // загружаем соответствующие критерии оценки для его категории.
        productComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadCriteriaForCategory(newValue.getCategoryId());
                    }
                }
        );
    }

    /**
     * Устанавливает отзыв для режима редактирования.
     * Этот метод вызывается извне перед отображением окна.
     */
    public void setReview(ReviewDto review) {
        this.review = review;
        // Откладываем обновление, чтобы UI успел загрузиться
        Platform.runLater(this::updateUiForReview);
    }

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Сохранить".
     * Валидирует данные, собирает их в DTO и отправляет на сервер.
     */
    @FXML
    private void handleSave() {
        if (productComboBox.getValue() == null || datePicker.getValue() == null) {
            errorLabel.setText("Товар и дата должны быть выбраны!");
            return;
        }

        // 1. Собираем основные данные
        ReviewDto dto = new ReviewDto();
        dto.setProductId(productComboBox.getValue().getId());
        dto.setDateCreated(datePicker.getValue().atStartOfDay());

        // 2. Собираем оценки по критериям
        Map<Long, Integer> ratings = new HashMap<>();
        for (Node node : criteriaGrid.getChildren()) {
            if (node instanceof Spinner) {
                Spinner<Integer> spinner = (Spinner<Integer>) node;
                Long criterionId = (Long) spinner.getUserData();
                Integer rating = spinner.getValue();
                ratings.put(criterionId, rating);
            }
        }
        dto.setRatings(ratings);

        // 3. Отправляем на сервер в фоновом потоке
        new Thread(() -> {
            try {
                if (review == null) {
                    reviewService.createReview(dto);
                } else {
                    reviewService.updateReview(review.getId(), dto);
                }
                Platform.runLater(() -> {
                    saveClicked = true;
                    dialogStage.close();
                });
            } catch (IOException e) {
                Platform.runLater(() -> errorLabel.setText("Ошибка сохранения: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Обрабатывает нажатие на кнопку "Отмена".
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    //================================================================================
    // Загрузка данных с сервера
    //================================================================================

    /**
     * Асинхронно загружает список всех товаров с сервера.
     */
    private void loadProducts() {
        new Thread(() -> {
            try {
                final List<ProductDto> products = dictionaryService.getAllProducts();
                Platform.runLater(() -> {
                    productComboBox.getItems().setAll(products);
                    updateUiForReview();
                });
            } catch (IOException e) {
                Platform.runLater(() -> AlertFactory.showError("Ошибка загрузки", "Не удалось загрузить список товаров."));
            }
        }).start();
    }

    /**
     * Асинхронно загружает критерии для выбранной категории.
     */
    private void loadCriteriaForCategory(Long categoryId) {
        new Thread(() -> {
            try {
                criteriaList = dictionaryService.getCriteriaByCategoryId(categoryId);
                Platform.runLater(() -> {
                    populateCriteria(criteriaList);
                    updateUiForReview();
                });
            } catch (IOException e) {
                Platform.runLater(() -> AlertFactory.showError("Ошибка загрузки", "Не удалось загрузить критерии для категории."));
            }
        }).start();
    }

    //================================================================================
    // Логика обновления UI
    //================================================================================

    /**
     * Заполняет UI данными.
     * Вызывается после загрузки продуктов и после загрузки критериев.
     */
    private void updateUiForReview() {
        if (review != null) {
            // Режим редактирования
            titleLabel.setText("Редактирование отзыва");
            datePicker.setValue(review.getDateCreated().toLocalDate());

            productComboBox.getItems().stream()
                    .filter(p -> p.getId().equals(review.getProductId()))
                    .findFirst()
                    .ifPresent(productComboBox::setValue);
            productComboBox.setDisable(true);

            // Заполняем Spinner'ы оценками из отзыва
            Map<Long, Integer> ratingsMap = review.getReviewRatings().stream()
                    .collect(Collectors.toMap(ReviewRatingDto::getCriterionId, ReviewRatingDto::getRating));

            for (Node node : criteriaGrid.getChildren()) {
                if (node instanceof Spinner) {
                    Spinner<Integer> spinner = (Spinner<Integer>) node;
                    Long criterionId = (Long) spinner.getUserData();
                    if (ratingsMap.containsKey(criterionId)) {
                        spinner.getValueFactory().setValue(ratingsMap.get(criterionId));
                    }
                }
            }
        }
    }

    /**
     * Динамически создает и добавляет поля для ввода оценок (Spinner) в GridPane.
     */
    private void populateCriteria(List<CriterionDto> criteria) {
        criteriaGrid.getChildren().clear();
        for (int i = 0; i < criteria.size(); i++) {
            CriterionDto criterion = criteria.get(i);
            Label label = new Label(criterion.getName() + ":");
            Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 3);
            ratingSpinner.setUserData(criterion.getId());
            criteriaGrid.add(label, 0, i);
            criteriaGrid.add(ratingSpinner, 1, i);
        }
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Настраивает отображение объектов ProductDto в ComboBox.
     */
    private void configureComboBox() {
        productComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProductDto product) {
                if (product == null) {
                    return "";
                }
                return String.format("[%s] %s (%s)",
                        product.getCategoryName(),
                        product.getName(),
                        product.getBrand()
                );
            }
            @Override
            public ProductDto fromString(String string) {
                return null;
            }
        });
    }
}