package com.github.stasangelov.reviewanalytics.client.controller.tabs;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.controller.dialog.ReviewEditDialogController;
import com.github.stasangelov.reviewanalytics.client.model.ReviewDto;
import com.github.stasangelov.reviewanalytics.client.service.ReviewService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
        productCol.setPrefWidth(300);

        TableColumn<ReviewDto, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
        dateCol.setPrefWidth(150);

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
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Не удалось загрузить отзывы.");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddReview() {
        boolean saved = showReviewEditDialog(null);
        if (saved) {
            loadReviews(); // Обновляем таблицу, если сохранили
        }
    }

    @FXML
    private void handleEditReview() {
        ReviewDto selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Отзыв не выбран.");
            alert.setContentText("Пожалуйста, выберите отзыв в таблице для редактирования.");
            alert.showAndWait();
            return;
        }
        boolean saved = showReviewEditDialog(selected);
        if (saved) {
            loadReviews();
        }
    }

    @FXML
    private void handleApproveReview() {
        // TODO: Реализовать логику одобрения
        System.out.println("Approve clicked");
    }

    @FXML
    private void handleRejectReview() {
        // TODO: Реализовать логику отклонения
        System.out.println("Reject clicked");
    }

    private boolean showReviewEditDialog(ReviewDto review) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource("review-edit-dialog.fxml"));
            VBox page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(review == null ? "Добавление отзыва" : "Редактирование отзыва");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Устанавливаем родительское окно, чтобы диалог открывался по центру
            dialogStage.initOwner(reviewTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            ReviewEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setReview(review); // Передаем отзыв (или null)

            dialogStage.showAndWait();
            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}