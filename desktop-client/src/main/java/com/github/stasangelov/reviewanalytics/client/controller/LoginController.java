package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.AuthRequest;
import com.github.stasangelov.reviewanalytics.client.model.AuthResponse;
import com.github.stasangelov.reviewanalytics.client.service.ApiCallback;
import com.github.stasangelov.reviewanalytics.client.service.AuthService;
import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Контроллер, отвечающий за логику окна входа ('login-view.fxml').
 * Он обрабатывает действия пользователя, такие как ввод email/пароля и нажатие кнопок.
 */

public class LoginController {

    private final AuthService authService = new AuthService();

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        AuthRequest request = new AuthRequest();
        request.setEmail(emailField.getText());
        request.setPassword(passwordField.getText());

        authService.login(request, new ApiCallback<>() {
            @Override
            public void onSuccess(AuthResponse result) {
                // Сохраняем токен в сессии
                SessionManager.getInstance().setToken(result.getToken());
                Platform.runLater(() -> {
                    // Переключаемся на главный экран
                    ViewSwitcher.switchToMainView(event);
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
    protected void onRegisterLinkClick(ActionEvent event) {
        ViewSwitcher.switchScene(event, "registration-view.fxml");
    }

    private void setInfoLabel(String message) {
        infoLabel.setText(message);
    }
}