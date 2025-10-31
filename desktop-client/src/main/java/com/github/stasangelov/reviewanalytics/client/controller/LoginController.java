package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.auth.AuthRequest;
import com.github.stasangelov.reviewanalytics.client.model.auth.AuthResponse;
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
 * Контроллер для окна входа в систему (`login-view.fxml`).
 * Отвечает за сбор учетных данных пользователя, отправку запроса на аутентификацию
 * и навигацию в зависимости от результата.
 */
public class LoginController {

    // --- FXML Поля ---
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    // --- Зависимости ---
    private final AuthService authService = new AuthService();

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Войти".
     * Валидирует введенные данные, отправляет их на сервер для аутентификации.
     * В случае успеха создает сессию и переключается на главный экран.
     */
    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        setInfoLabel(null); // Скрываем предыдущие сообщения об ошибках

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            setInfoLabel("Все поля должны быть заполнены");
            return;
        }

        AuthRequest request = new AuthRequest();
        request.setEmail(emailField.getText());
        request.setPassword(passwordField.getText());

        // Асинхронный вызов сервиса аутентификации
        authService.login(request, new ApiCallback<>() {
            @Override
            public void onSuccess(AuthResponse result) {
                SessionManager.getInstance().createSession(result.getToken(), result.getRoles());
                Platform.runLater(() -> ViewSwitcher.switchToMainView(event));
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

    /**
     * Обрабатывает нажатие на гиперссылку "Зарегистрироваться".
     * Переключает сцену на окно регистрации.
     */
    @FXML
    protected void onRegisterLinkClick(ActionEvent event) {
        ViewSwitcher.switchScene(event, "registration-view.fxml");
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Управляет видимостью и текстом метки для отображения ошибок (`infoLabel`).
     */
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