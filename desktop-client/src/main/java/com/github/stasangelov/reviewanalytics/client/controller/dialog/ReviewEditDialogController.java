package com.github.stasangelov.reviewanalytics.client.controller.dialog;

import com.github.stasangelov.reviewanalytics.client.model.CriterionDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductDto;
import com.github.stasangelov.reviewanalytics.client.model.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.service.DictionaryService;
import com.github.stasangelov.reviewanalytics.client.service.ReviewService;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewEditDialogController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<ProductDto> productComboBox;
    @FXML private DatePicker datePicker;
    @FXML private GridPane criteriaGrid;
    @FXML private Label errorLabel;

    private final ReviewService reviewService = new ReviewService();
    private ReviewDto review;
    @Getter
    private boolean saveClicked = false;

    @Setter
    private Stage dialogStage;
    private final DictionaryService dictionaryService = new DictionaryService();

    public void setReview(ReviewDto review) {
        this.review = review;
        // TODO: Заполнить поля данными из review, если это режим редактирования
    }

    /**
     * Метод, который вызывается после загрузки FXML.
     * Запускаем загрузку справочников.
     */
    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now()); // Устанавливаем текущую дату по умолчанию
        configureComboBox();
        loadDictionaries();
    }

    /**
     * Настраивает ComboBox для корректного отображения названий товаров.
     */
    private void configureComboBox() {
        productComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProductDto product) {
                return product == null ? "" : product.getName() + " (" + product.getBrand() + ")";
            }
            @Override
            public ProductDto fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Асинхронно загружает списки товаров и критериев с сервера.
     */
    private void loadDictionaries() {
        new Thread(() -> {
            try {
                final List<ProductDto> products = dictionaryService.getAllProducts();
                final List<CriterionDto> criteria = dictionaryService.getAllCriteria();
                Platform.runLater(() -> {
                    productComboBox.getItems().setAll(products);
                    populateCriteria(criteria);
                });
            } catch (IOException e) {
                Platform.runLater(() -> errorLabel.setText("Ошибка загрузки справочников."));
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Динамически создает поля для ввода оценок на основе списка критериев.
     */
    private void populateCriteria(List<CriterionDto> criteria) {
        criteriaGrid.getChildren().clear();
        for (int i = 0; i < criteria.size(); i++) {
            CriterionDto criterion = criteria.get(i);
            Label label = new Label(criterion.getName() + ":");
            // Мы будем использовать Spinner для удобного ввода чисел от 1 до 5
            Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 3); // от 1 до 5, по умолчанию 3
            ratingSpinner.setUserData(criterion.getId()); // Сохраняем ID критерия в элементе UI

            criteriaGrid.add(label, 0, i);
            criteriaGrid.add(ratingSpinner, 1, i);
        }
    }

    @FXML
    private void handleSave() {
        if (productComboBox.getValue() == null || datePicker.getValue() == null) {
            errorLabel.setText("Товар и дата должны быть выбраны!");
            return;
        }

        // Собираем данные из формы
        ReviewDto dto = new ReviewDto();
        dto.setProductId(productComboBox.getValue().getId());
        dto.setDateCreated(datePicker.getValue());

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

        new Thread(() -> {
            try {
                // TODO: Вызывать update, если это режим редактирования
                reviewService.createReview(dto);
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

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}