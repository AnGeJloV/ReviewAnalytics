package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.client.model.UserDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiCallback;
import com.github.stasangelov.reviewanalytics.client.service.AuthService;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Контроллер для окна регистрации ('registration-view.fxml').
 * Отвечает за сбор данных нового пользователя (имя, email, пароль),
 * отправку запроса на регистрацию через {@link AuthService} и обработку ответа от сервера.
 * В случае успеха информирует пользователя и перенаправляет его на экран входа.
 */

public class RegistrationController {

    private final AuthService authService = new AuthService();

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) {

        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            setInfoLabel("Все поля должны быть заполнены");
            return;
        }

        RegistrationRequest request = new RegistrationRequest();
        request.setName(nameField.getText());
        request.setEmail(emailField.getText());
        request.setPassword(passwordField.getText());

        authService.register(request, new ApiCallback<>() {
            @Override
            public void onSuccess(UserDto result) {
                Platform.runLater(() -> {
                    // Показываем всплывающее окно об успехе
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText("Регистрация прошла успешно!");
                    alert.setContentText("Пользователь " + result.getName() + " был создан. Теперь вы можете войти.");
                    alert.showAndWait();

                    // Возвращаемся на экран входа
                    ViewSwitcher.switchScene(event, "login-view.fxml");
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> setInfoLabel("Ошибка сети: " + e.getMessage()));
            }

            @Override
            public void onError(int code, String message) {
                Platform.runLater(() -> setInfoLabel("Ошибка (" + code + "): " + message));
            }
        });
    }

    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        ViewSwitcher.switchScene(event, "login-view.fxml");
    }

    private void setInfoLabel(String message) {
        infoLabel.setText(message);
    }
}