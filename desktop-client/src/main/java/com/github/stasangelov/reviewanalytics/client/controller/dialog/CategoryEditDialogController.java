package com.github.stasangelov.reviewanalytics.client.controller.dialog;

import com.github.stasangelov.reviewanalytics.client.model.CategoryDto;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

/**
 * Контроллер для диалогового окна добавления/редактирования категории.
 */

public class CategoryEditDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private Label errorLabel;

    @Setter
    private Stage dialogStage;
    private CategoryDto category;
    @Getter
    private boolean saveClicked = false;

    /**
     * Заполняет поля данными существующей категории для редактирования.
     * Если передать null, окно будет работать в режиме создания.
     */
    public void setCategory(CategoryDto category) {
        this.category = category;
        if (category != null && category.getId() != null) {
            titleLabel.setText("Редактирование категории");
            nameField.setText(category.getName());
        } else {
            titleLabel.setText("Добавление новой категории");
            this.category = new CategoryDto();
        }
    }

    /**
     * Обработчик нажатия кнопки "Сохранить".
     * Проверяет ввод, обновляет DTO и закрывает окно.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            category.setName(nameField.getText());
            saveClicked = true;
            dialogStage.close();
        }
    }

    // Обработчик нажатия кнопки "Отмена".
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    // Простая проверка, что поле не пустое.
    private boolean isInputValid() {
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errorLabel.setText("Название не может быть пустым!");
            return false;
        }
        errorLabel.setText("");
        return true;
    }
}