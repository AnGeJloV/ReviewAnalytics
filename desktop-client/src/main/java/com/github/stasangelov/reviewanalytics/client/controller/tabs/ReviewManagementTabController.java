package com.github.stasangelov.reviewanalytics.client.controller.tabs;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.controller.dialog.ReviewEditDialogController;
import com.github.stasangelov.reviewanalytics.client.model.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiException;
import com.github.stasangelov.reviewanalytics.client.service.ReviewService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReviewManagementTabController {

    @FXML
    private TableView<ReviewDto> reviewTable;
    private final ReviewService reviewService = new ReviewService();

    @FXML
    public void initialize() {
        setupTable();
        loadReviews();
    }

    private void setupTable() {
        TableColumn<ReviewDto, String> productCol = new TableColumn<>("Товар");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.setPrefWidth(350);

        TableColumn<ReviewDto, LocalDateTime> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
        dateCol.setPrefWidth(200);

        TableColumn<ReviewDto, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        reviewTable.getColumns().setAll(productCol, dateCol, statusCol);
    }

    private void loadReviews() {
        new Thread(() -> {
            try {
                final List<ReviewDto> reviews = reviewService.getAllReviews();
                Platform.runLater(() -> reviewTable.setItems(FXCollections.observableArrayList(reviews)));
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Ошибка сети", "Не удалось загрузить отзывы.", e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddReview() {
        boolean saved = showReviewEditDialog(null);
        if (saved) {
            loadReviews();
        }
    }

    @FXML
    private void handleEditReview() {
        ReviewDto selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoAlert("Ничего не выбрано", "Пожалуйста, выберите отзыв для редактирования.");
            return;
        }
        boolean saved = showReviewEditDialog(selected);
        if (saved) {
            loadReviews();
        }
    }

    @FXML
    private void handleApproveReview() {
        changeSelectedReviewStatus("ACTIVE");
    }

    @FXML
    private void handleRejectReview() {
        changeSelectedReviewStatus("REJECTED");
    }

    private void changeSelectedReviewStatus(String status) {
        ReviewDto selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoAlert("Ничего не выбрано", "Пожалуйста, выберите отзыв для изменения статуса.");
            return;
        }

        new Thread(() -> {
            try {
                reviewService.changeStatus(selected.getId(), status);
                Platform.runLater(this::loadReviews);
            } catch (ApiException e) {
                Platform.runLater(() -> showErrorAlert("Ошибка API (" + e.getStatusCode() + ")", "Не удалось изменить статус.", e.getMessage()));
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Ошибка сети", "Не удалось изменить статус.", e.getMessage()));
            }
        }).start();
    }

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

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}