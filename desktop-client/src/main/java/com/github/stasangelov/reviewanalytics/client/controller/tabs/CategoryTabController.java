package com.github.stasangelov.reviewanalytics.client.controller.tabs;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.controller.dialog.CategoryEditDialogController;
import com.github.stasangelov.reviewanalytics.client.model.CategoryDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiException;
import com.github.stasangelov.reviewanalytics.client.service.CategoryService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер, управляющий логикой вкладки "Категории".
 */
public class CategoryTabController {

    @FXML
    private TableView<CategoryDto> categoryTable;

    private final CategoryService categoryService = new CategoryService();

    /**
     * Метод initialize() вызывается JavaFX после того, как FXML-файл загружен.
     */
    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                final List<CategoryDto> categories = categoryService.getAllCategories();
                Platform.runLater(() -> categoryTable.setItems(FXCollections.observableArrayList(categories)));
            } catch (ApiException e) {
                Platform.runLater(() -> showErrorAlert("Ошибка API", e.getMessage()));
                e.printStackTrace();
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Ошибка сети", "Не удалось загрузить список категорий. Проверьте подключение к серверу."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAdd() {
        CategoryDto newCategory = new CategoryDto();
        boolean saveClicked = showCategoryEditDialog(newCategory);
        if (saveClicked) {
            new Thread(() -> {
                try {
                    categoryService.createCategory(newCategory);
                    Platform.runLater(this::loadCategories);
                } catch (ApiException e) {
                    Platform.runLater(() -> showErrorAlert("Ошибка API (" + e.getStatusCode() + ")", e.getMessage()));
                } catch (IOException e) {
                    Platform.runLater(() -> showErrorAlert("Ошибка создания", "Не удалось создать категорию."));
                }
            }).start();
        }
    }

    @FXML
    private void handleEdit() {
        CategoryDto selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            boolean saveClicked = showCategoryEditDialog(selectedCategory);
            if (saveClicked) {
                new Thread(() -> {
                    try {
                        categoryService.updateCategory(selectedCategory);
                        Platform.runLater(this::loadCategories);
                    } catch (ApiException e) {
                        Platform.runLater(() -> showErrorAlert("Ошибка API (" + e.getStatusCode() + ")", e.getMessage()));
                    } catch (IOException e) {
                        Platform.runLater(() -> showErrorAlert("Ошибка обновления", "Не удалось обновить категорию."));
                    }
                }).start();
            }
        } else {
            showInfoAlert("Ничего не выбрано", "Пожалуйста, выберите категорию для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        CategoryDto selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить категорию '" + selectedCategory.getName() + "'?");
            alert.setContentText("Это действие нельзя будет отменить.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        categoryService.deleteCategory(selectedCategory.getId());
                        Platform.runLater(this::loadCategories);
                    } catch (ApiException e) {
                        Platform.runLater(() -> showErrorAlert("Ошибка API (" + e.getStatusCode() + ")", e.getMessage()));
                    } catch (IOException e) {
                        Platform.runLater(() -> showErrorAlert("Ошибка удаления", "Не удалось удалить категорию."));
                    }
                }).start();
            }
        } else {
            showInfoAlert("Ничего не выбрано", "Пожалуйста, выберите категорию для удаления.");
        }
    }

     // Вспомогательный метод для отображения и управления диалоговым окном.
    private boolean showCategoryEditDialog(CategoryDto category) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource("category-edit-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            if (category.getId() != null) {
                dialogStage.setTitle("Редактирование категории");
            } else {
                dialogStage.setTitle("Добавление новой категории");
            }
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            CategoryEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCategory(category);

            dialogStage.showAndWait();

            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
