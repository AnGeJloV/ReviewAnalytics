package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.client.model.UserDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiCallback;
import com.github.stasangelov.reviewanalytics.client.service.AuthService;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {

    private final AuthService authService = new AuthService();

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) {
        // Сначала скрываем старые ошибки
        setInfoLabel(null);

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
                    AlertFactory.showInfo("Регистрация прошла успешно!", "Пользователь " + result.getName() + " был создан. Теперь вы можете войти.");
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
        if (message == null || message.isEmpty()) {
            infoLabel.setText("");
            infoLabel.setManaged(false);
            infoLabel.setVisible(false);
        } else {
            infoLabel.setText(message);
            infoLabel.setManaged(true);
            infoLabel.setVisible(true);
        }
    }
}