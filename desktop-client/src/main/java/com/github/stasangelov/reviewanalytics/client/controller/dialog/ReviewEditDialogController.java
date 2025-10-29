package com.github.stasangelov.reviewanalytics.client.controller.dialog;

import com.github.stasangelov.reviewanalytics.client.model.CriterionDto;
import com.github.stasangelov.reviewanalytics.client.model.ProductDto;
import com.github.stasangelov.reviewanalytics.client.model.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.model.ReviewRatingDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReviewEditDialogController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<ProductDto> productComboBox;
    @FXML private DatePicker datePicker;
    @FXML private GridPane criteriaGrid;
    @FXML private Label errorLabel;

    @Setter private Stage dialogStage;
    @Getter private boolean saveClicked = false;

    private final DictionaryService dictionaryService = new DictionaryService();
    private final ReviewService reviewService = new ReviewService();
    private ReviewDto review;
    private List<CriterionDto> criteriaList;

    @FXML
    private void initialize() {
        datePicker.setValue(java.time.LocalDate.now());
        configureComboBox();
        loadDictionaries();
    }

    public void setReview(ReviewDto review) {
        this.review = review;
        Platform.runLater(this::updateUiForReview); // Откладываем обновление, чтобы UI успел загрузиться
    }

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

    private void loadDictionaries() {
        new Thread(() -> {
            try {
                final List<ProductDto> products = dictionaryService.getAllProducts();
                criteriaList = dictionaryService.getAllCriteria();
                Platform.runLater(() -> {
                    productComboBox.getItems().setAll(products);
                    populateCriteria(criteriaList);
                    updateUiForReview(); // Вызываем еще раз, если данные отзыва пришли раньше
                });
            } catch (IOException e) {
                Platform.runLater(() -> errorLabel.setText("Ошибка загрузки справочников."));
                e.printStackTrace();
            }
        }).start();
    }

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

    @FXML
    private void handleSave() {
        if (productComboBox.getValue() == null || datePicker.getValue() == null) {
            errorLabel.setText("Товар и дата должны быть выбраны!");
            return;
        }

        ReviewDto dto = new ReviewDto();
        dto.setProductId(productComboBox.getValue().getId());
        dto.setDateCreated(datePicker.getValue().atStartOfDay());

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

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}